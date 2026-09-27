#!/usr/bin/env bash
# =====================================================================
#  sql-to-entities.sh
# ---------------------------------------------------------------------
#  MỤC ĐÍCH:
#    Đọc một file SQL (PostgreSQL DDL), tự động sinh Java JPA Entity
#    cho từng bảng CREATE TABLE tìm thấy.
#
#  CÁCH DÙNG (KHÔNG truyền argument):
#    ./sql-to-entities.sh
#    Script sẽ HỎI LẦN LƯỢT khi chạy:
#      1) Đường dẫn tuyệt đối của file SQL đầu vào
#      2) Đường dẫn tuyệt đối của thư mục Entity đầu ra
#
#  PACKAGE:
#    KHÔNG hỏi package riêng. Package được SUY RA TỪ THƯ MỤC ĐẦU RA:
#      /xxx/src/main/java/org/software/open/source/audio/io/entities
#      -> org.software.open.source.audio.io.entities
#    Nếu đường dẫn không chứa /src/main/java/ thì mới hỏi package thủ công.
#
#  HÀNH VI VỚI BaseEntity:
#    - Biến BASE_ENTITY_FILE_NAME quyết định tên file cần kiểm tra
#      TRONG THƯ MỤC ĐẦU RA.
#    - Đã có  -> entity extends nó, không sinh trùng.
#    - Chưa có -> tự tạo BaseEntity.java (isActive, createdAt, updatedAt,
#      KHÔNG có id vì id mỗi bảng có thể khác kiểu).
#    - Các cột is_active / created_at / updated_at trong SQL bị bỏ qua
#      ở entity con vì đã thuộc về BaseEntity.
#
#  QUY TẮC SINH:
#    - Tên class = PascalCase(ten_bang) + "Entity"
#    - Khóa chính   -> @Id (+ IDENTITY nếu serial/bigserial)
#    - Khóa ngoại   -> @ManyToOne + @JoinColumn, bảng cha tự thêm @OneToMany
#    - CONSTRAINT UNIQUE -> @Table(uniqueConstraints = ...)
#    - CREATE INDEX thường -> @Table(indexes = ...)
#    - CHECK / DEFAULT / partial index -> bỏ qua
#
#  CHỐNG GHI ĐÈ:
#    Mặc định không ghi đè entity đã tồn tại.
#    Ghi đè:  FORCE=1 ./sql-to-entities.sh
# =====================================================================

set -euo pipefail

# =====================================================================
# 1. BIẾN CẤU HÌNH (ĐỔI TẠI ĐÂY NẾU CẦN)
# =====================================================================

# Tên file BaseEntity cần kiểm tra trong thư mục đầu ra
BASE_ENTITY_FILE_NAME="BaseEntity.java"

# Tên class BaseEntity (phải khớp nội dung file)
BASE_ENTITY_CLASS_NAME="BaseEntity"

# Package dự phòng, chỉ dùng khi đường dẫn không suy ra được package
DEFAULT_PACKAGE="org.software.open.source.audio.io.entities"

# Các cột đã thuộc về BaseEntity -> không sinh lại trong entity con
BASE_COLUMNS="is_active created_at updated_at"

# =====================================================================
# 2. HỎI ĐƯỜNG DẪN KHI CHẠY
# =====================================================================

echo "=================================================="
echo "  SQL -> JAVA ENTITY GENERATOR"
echo "=================================================="

# ---- 2.1. Hỏi file SQL đầu vào (hỏi lại tới khi hợp lệ) ----
SQL_FILE=""
while [ ! -f "${SQL_FILE}" ]; do
    read -r -p "Nhap duong dan tuyet doi cua file SQL dau vao: " SQL_FILE
    if [ ! -f "${SQL_FILE}" ]; then
        echo "  !! File khong ton tai: ${SQL_FILE}"
    fi
done

# ---- 2.2. Hỏi thư mục Entity đầu ra ----
OUT_DIR=""
while [ -z "${OUT_DIR}" ]; do
    read -r -p "Nhap duong dan tuyet doi thu muc Entity dau ra: " OUT_DIR
    if [ -z "${OUT_DIR}" ]; then
        echo "  !! Duong dan khong duoc de trong"
    fi
done

# Bỏ slash cuối rồi tạo thư mục nếu chưa có
OUT_DIR="${OUT_DIR%/}"
mkdir -p "${OUT_DIR}"

# =====================================================================
# 3. SUY RA PACKAGE TỪ CHÍNH THƯ MỤC ĐẦU RA
#    /.../src/main/java/org/software/.../entities
#    -> org.software...entities
# =====================================================================
PACKAGE=""
if [[ "${OUT_DIR}" == *"/src/main/java/"* ]]; then
    PACKAGE="${OUT_DIR#*/src/main/java/}"
    PACKAGE="${PACKAGE//\//.}"
fi

if [ -z "${PACKAGE}" ]; then
    echo "Khong suy duoc package tu duong dan (thieu /src/main/java/)."
    read -r -p "Nhap package thu cong (Enter = ${DEFAULT_PACKAGE}): " PACKAGE
    PACKAGE="${PACKAGE:-$DEFAULT_PACKAGE}"
fi

echo "Package su dung: ${PACKAGE}"

# =====================================================================
# 4. HÀM TIỆN ÍCH
# =====================================================================

trim() {
    local s="$1"
    s="${s#"${s%%[![:space:]]*}"}"
    s="${s%"${s##*[![:space:]]}"}"
    printf '%s' "${s}"
}

# snake_case -> camelCase
camel() {
    printf '%s' "$1" | awk -F'_' '{
        printf "%s", $1
        for (i = 2; i <= NF; i++)
            printf "%s%s", toupper(substr($i, 1, 1)), substr($i, 2)
    }'
}

# snake_case -> PascalCase
pascal() {
    local c
    c="$(camel "$1")"
    printf '%s%s' "$(printf '%s' "${c:0:1}" | tr '[:lower:]' '[:upper:]')" "${c:1}"
}

# Kiểu SQL -> kiểu Java
map_type() {
    case "$1" in
        bigserial|bigint|int8)          printf 'Long' ;;
        serial|integer|int|int4)        printf 'Integer' ;;
        smallint|int2)                  printf 'Integer' ;;
        varchar|character|text|bpchar)  printf 'String' ;;
        boolean|bool)                   printf 'Boolean' ;;
        timestamptz|timestamp)          printf 'Instant' ;;
        date)                           printf 'LocalDate' ;;
        numeric|decimal)                printf 'BigDecimal' ;;
        double|float8)                  printf 'Double' ;;
        real|float4)                    printf 'Float' ;;
        uuid)                           printf 'UUID' ;;
        *)                              printf 'String' ;;
    esac
}

# Tách thân CREATE TABLE theo dấu phẩy ở cấp ngoặc đơn bằng 0
split_top_level() {
    printf '%s' "$1" | awk '{
        depth = 0; item = ""
        n = length($0)
        for (i = 1; i <= n; i++) {
            c = substr($0, i, 1)
            if (c == "(") { depth++; item = item c; continue }
            if (c == ")") { depth--; item = item c; continue }
            if (c == "," && depth == 0) { print item; item = ""; continue }
            item = item c
        }
        if (item != "") print item
    }'
}

write_file() {
    local path="$1"
    if [ -e "${path}" ] && [ "${FORCE:-0}" != "1" ]; then
        echo "SKIP (da ton tai): ${path}"
        return
    fi
    cat > "${path}"
    echo "CREATED: ${path}"
}

# =====================================================================
# 5. KIỂM TRA / TẠO BaseEntity TRONG THƯ MỤC ĐẦU RA
# =====================================================================

BASE_PATH="${OUT_DIR}/${BASE_ENTITY_FILE_NAME}"

if [ -f "${BASE_PATH}" ]; then
    echo "BaseEntity da ton tai: ${BASE_PATH} -> entity se extends"
else
    write_file "${BASE_PATH}" <<EOF
package ${PACKAGE};

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public abstract class ${BASE_ENTITY_CLASS_NAME} {

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
  
}

EOF
fi

# =====================================================================
# 6. CHUẨN HÓA SQL
# =====================================================================

TMP_SQL="$(mktemp)"
trap 'rm -f "${TMP_SQL}"' EXIT

sed -e 's/--.*$//' -e 's|/\*[^*]*\*/||g' "${SQL_FILE}" \
    | tr '\n' ' ' \
    | tr -s ' ' > "${TMP_SQL}"

# =====================================================================
# 7. PARSE CREATE TABLE / CREATE INDEX
# =====================================================================

declare -a TABLES=()
declare -A COLS=() CTYPE=() CLEN=() CNULL=() PK=() FKS=() UQS=() IDX=() GFK=()

while IFS= read -r stmt; do
    stmt="$(trim "${stmt}")"
    [ -z "${stmt}" ] && continue
    up="$(printf '%s' "${stmt}" | tr '[:lower:]' '[:upper:]')"

    case "${up}" in
    CREATE\ TABLE*)
        [[ "${stmt}" =~ CREATE[[:space:]]+TABLE[[:space:]]+(IF[[:space:]]+NOT[[:space:]]+EXISTS[[:space:]]+)?([A-Za-z0-9_\".]+) ]] || continue
        tbl="$(printf '%s' "${BASH_REMATCH[2]##*.}" | tr -d '\"')"
        inner="${stmt#*\(}"
        inner="${inner%)*}"
        TABLES+=("${tbl}")
        COLS["${tbl}"]=""

        while IFS= read -r item; do
            item="$(trim "${item}")"
            [ -z "${item}" ] && continue
            iup="$(printf '%s' "${item}" | tr '[:lower:]' '[:upper:]')"

            case "${iup}" in
            CONSTRAINT*)
                if [[ "${item}" =~ PRIMARY[[:space:]]+KEY[[:space:]]*\(([^\)]*)\) ]]; then
                    PK["${tbl}"]="$(printf '%s' "${BASH_REMATCH[1]}" | tr -d ' ')"
                elif [[ "${item}" =~ FOREIGN[[:space:]]+KEY[[:space:]]*\(([^\)]*)\)[[:space:]]+REFERENCES[[:space:]]+([A-Za-z0-9_\".]+) ]]; then
                    fcol="$(printf '%s' "${BASH_REMATCH[1]}" | tr -d ' ')"
                    reft="$(printf '%s' "${BASH_REMATCH[2]##*.}" | tr -d '\"')"
                    fkname=""
                    [[ "${item}" =~ CONSTRAINT[[:space:]]+([A-Za-z0-9_]+) ]] && fkname="${BASH_REMATCH[1]}"
                    FKS["${tbl}"]+="${fkname}|${fcol}|${reft}"$'\n'
                    GFK["${reft}"]+="${tbl}|${fcol}"$'\n'
                elif [[ "${item}" =~ UNIQUE[[:space:]]*\(([^\)]*)\) ]]; then
                    uqname=""
                    [[ "${item}" =~ CONSTRAINT[[:space:]]+([A-Za-z0-9_]+) ]] && uqname="${BASH_REMATCH[1]}"
                    UQS["${tbl}"]+="${uqname}|$(printf '%s' "${BASH_REMATCH[1]}" | tr -d ' ')"$'\n'
                fi
                ;;
            PRIMARY\ KEY*)
                [[ "${item}" =~ \(([^\)]*)\) ]] && PK["${tbl}"]="$(printf '%s' "${BASH_REMATCH[1]}" | tr -d ' ')"
                ;;
            UNIQUE*)
                if [[ "${item}" =~ UNIQUE[[:space:]]*\(([^\)]*)\) ]]; then
                    ucols="$(printf '%s' "${BASH_REMATCH[1]}" | tr -d ' ')"
                    UQS["${tbl}"]+="uq_${tbl}_$(printf '%s' "${ucols}" | tr ',' '_')|${ucols}"$'\n'
                fi
                ;;
            CHECK*|EXCLUDE*)
                # CHECK khong map sang JPA -> bo qua
                ;;
            *)
                read -r cname ctype crest <<< "${item}" || true
                cname="$(printf '%s' "${cname}" | tr -d '\"')"
                ctype="$(printf '%s' "${ctype}" | tr -d '\"' | tr '[:upper:]' '[:lower:]')"
                [ "${ctype}" = "character" ] && ctype="varchar"
                len=""
                if [ "${ctype}" = "varchar" ] && [[ "${item}" =~ \(([0-9]+)\) ]]; then
                    len="${BASH_REMATCH[1]}"
                fi
                nn=0
                [[ "${iup}" == *NOT[[:space:]]NULL* ]] && nn=1
                COLS["${tbl}"]+="${cname}"$'\n'
                CTYPE["${tbl}.${cname}"]="${ctype}"
                CLEN["${tbl}.${cname}"]="${len}"
                CNULL["${tbl}.${cname}"]="${nn}"
                if [[ "${iup}" == *PRIMARY[[:space:]]KEY* ]]; then
                    PK["${tbl}"]="${cname}"
                fi
                if [[ "${iup}" == *REFERENCES* ]]; then
                    if [[ "${item}" =~ REFERENCES[[:space:]]+([A-Za-z0-9_\".]+) ]]; then
                        reft="$(printf '%s' "${BASH_REMATCH[1]##*.}" | tr -d '\"')"
                        FKS["${tbl}"]+="|${cname}|${reft}"$'\n'
                        GFK["${reft}"]+="${tbl}|${cname}"$'\n'
                    fi
                fi
                ;;
            esac
        done < <(split_top_level "${inner}")
        ;;

    CREATE\ INDEX*|CREATE\ UNIQUE\ INDEX*)
        # Partial index (co WHERE) khong bieu dien duoc trong JPA -> bo qua
        [[ "${up}" == *WHERE* ]] && continue
        if [[ "${stmt}" =~ INDEX[[:space:]]+([A-Za-z0-9_]+)[[:space:]]+ON[[:space:]]+([A-Za-z0-9_\".]+)[[:space:]]*\(([^\)]*)\) ]]; then
            iname="${BASH_REMATCH[1]}"
            itbl="$(printf '%s' "${BASH_REMATCH[2]##*.}" | tr -d '\"')"
            icols="$(printf '%s' "${BASH_REMATCH[3]}" | tr -d ' ')"
            IDX["${itbl}"]+="${iname}|${icols}"$'\n'
        fi
        ;;
    esac
done < <(awk 'BEGIN{RS=";"} {print $0 ";"}' "${TMP_SQL}")

# =====================================================================
# 8. SINH JAVA ENTITY CHO TỪNG BẢNG
# =====================================================================

for tbl in "${TABLES[@]}"; do
    cls="$(pascal "${tbl}")Entity"
    pk="${PK[${tbl}]:-}"

    # ---- 8.1. Cột khóa ngoại (để loại khỏi field thường) ----
    fkcols=""
    while IFS='|' read -r fn fc fr; do
        [ -z "${fc}" ] && continue
        fkcols+=" ${fc}"
    done <<< "${FKS[${tbl}]:-}"

    FIELDS=""

    # ---- 8.2. Khóa chính ----
    if [ -n "${pk}" ]; then
        ptype="$(map_type "${CTYPE[${tbl}.${pk}]:-bigint}")"
        FIELDS+="    @Id"$'\n'
        case "${CTYPE[${tbl}.${pk}]:-}" in
            serial|bigserial)
                FIELDS+="    @GeneratedValue(strategy = GenerationType.IDENTITY)"$'\n'
                ;;
        esac
        FIELDS+="    private ${ptype} $(camel "${pk}");"$'\n\n'
    fi

    # ---- 8.3. Cột thường (bỏ cột BaseEntity, bỏ PK, bỏ FK) ----
    while IFS= read -r c; do
        [ -z "${c}" ] && continue
        case " ${BASE_COLUMNS} " in *" ${c} "*) continue ;; esac
        [ "${c}" = "${pk}" ] && continue
        case "${fkcols}" in *" ${c} "*) continue ;; esac

        lt="${CTYPE[${tbl}.${c}]}"
        jt="$(map_type "${lt}")"
        ann="    @Column(name = \"${c}\""
        [ "${CNULL[${tbl}.${c}]}" = "1" ] && ann+=", nullable = false"
        if [ "${lt}" = "varchar" ] && [ -n "${CLEN[${tbl}.${c}]:-}" ]; then
            ann+=", length = ${CLEN[${tbl}.${c}]}"
        fi
        [ "${lt}" = "text" ] && ann+=", columnDefinition = \"text\""
        ann+=")"
        FIELDS+="${ann}"$'\n'"    private ${jt} $(camel "${c}");"$'\n\n'
    done <<< "${COLS[${tbl}]:-}"

    # ---- 8.4. Khóa ngoại -> @ManyToOne ----
    while IFS='|' read -r fn fc fr; do
        [ -z "${fc}" ] && continue
        fname="$(camel "${fc%_id}")"
        pcls="$(pascal "${fr}")Entity"
        nn="${CNULL[${tbl}.${fc}]:-0}"

        m="    @ManyToOne(fetch = FetchType.LAZY"
        [ "${nn}" = "1" ] && m+=", optional = false"
        m+=")"$'\n'
        j="    @JoinColumn(name = \"${fc}\""
        [ "${nn}" = "1" ] && j+=", nullable = false"
        [ -n "${fn}" ] && j+=", foreignKey = @ForeignKey(name = \"${fn}\")"
        j+=")"$'\n'
        FIELDS+="${m}${j}    private ${pcls} ${fname};"$'\n\n'
    done <<< "${FKS[${tbl}]:-}"

    # ---- 8.5. Chiều ngược lại -> @OneToMany trên bảng cha ----
    while IFS='|' read -r ct fc; do
        [ -z "${ct}" ] && continue
        childfield="$(camel "${fc%_id}")"
        ccls="$(pascal "${ct}")Entity"
        FIELDS+="    @OneToMany(mappedBy = \"${childfield}\", fetch = FetchType.LAZY)"$'\n'
        FIELDS+="    private List<${ccls}> $(camel "${ct}") = new ArrayList<>();"$'\n\n'
    done <<< "${GFK[${tbl}]:-}"

    # ---- 8.6. @Table(uniqueConstraints, indexes) ----
    uc=""
    while IFS='|' read -r un ucs; do
        [ -z "${un}" ] && continue
        pretty="${ucs//,/\", \"}"
        uc+="        @UniqueConstraint(name = \"${un}\", columnNames = {\"${pretty}\"}),"$'\n'
    done <<< "${UQS[${tbl}]:-}"
    uc="${uc%,$'\n'}"

    ix=""
    while IFS='|' read -r iname icols; do
        [ -z "${iname}" ] && continue
        pretty="${icols//,/, }"
        ix+="        @Index(name = \"${iname}\", columnList = \"${pretty}\"),"$'\n'
    done <<< "${IDX[${tbl}]:-}"
    ix="${ix%,$'\n'}"

    if [ -n "${uc}" ] || [ -n "${ix}" ]; then
        TANN="@Table(name = \"${tbl}\""
        [ -n "${uc}" ] && TANN+=", uniqueConstraints = {"$'\n'"${uc}"$'\n'"    }"
        [ -n "${ix}" ] && TANN+=", indexes = {"$'\n'"${ix}"$'\n'"    }"
        TANN+=")"
    else
        TANN="@Table(name = \"${tbl}\")"
    fi

    # ---- 8.7. Import: bộ cố định + bổ sung theo kiểu dữ liệu xuất hiện ----
    IMP="import java.util.ArrayList;
import java.util.List;
"
    case "${FIELDS}" in *Instant*)    IMP+="import java.time.Instant;"$'\n' ;; esac
    case "${FIELDS}" in *LocalDate*)  IMP+="import java.time.LocalDate;"$'\n' ;; esac
    case "${FIELDS}" in *BigDecimal*) IMP+="import java.math.BigDecimal;"$'\n' ;; esac
    case "${FIELDS}" in *UUID*)       IMP+="import java.util.UUID;"$'\n' ;; esac
    IMP+="
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
"

    # ---- 8.8. Ghi file ----
    write_file "${OUT_DIR}/${cls}.java" <<EOF
package ${PACKAGE};

${IMP}
@Getter
@Setter
@Entity
${TANN}
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ${cls} extends ${BASE_ENTITY_CLASS_NAME} {

${FIELDS}}
EOF

done

echo "=================================================="
echo "HOAN TAT: sinh entity tu ${SQL_FILE}"
echo "Thu muc dau ra : ${OUT_DIR}"
echo "Package        : ${PACKAGE}"
echo "=================================================="