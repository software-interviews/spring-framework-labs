#!/usr/bin/env bash
# =====================================================================
#  03-entity-to-request.sh
# ---------------------------------------------------------------------
#  MỤC ĐÍCH:
#    Đọc một file Java JPA Entity, tự động sinh file Java Request/DTO
#    tương ứng (bỏ các annotation JPA, giữ lại field).
#
#  CÁCH DÙNG (KHÔNG truyền argument):
#    ./03-entity-to-request.sh
#    Script sẽ HỎI LẦN LƯỢT khi chạy:
#      1) Đường dẫn tuyệt đối của file Java Entity đầu vào
#      2) Đường dẫn tuyệt đối của thư mục Request đầu ra
#
#  PACKAGE:
#    KHÔNG hỏi package riêng. Package được SUY RA TỪ THƯ MỤC ĐẦU RA:
#      /xxx/src/main/java/org/software/.../requests
#      -> org.software...requests
#    Nếu đường dẫn không chứa /src/main/java/ thì mới hỏi package thủ công.
#
#  QUY TẮC SINH:
#    - Tên class: Bỏ "Entity" ở cuối (nếu có) và thêm "Request".
#      VD: UserEntity.java -> UserRequest.java
#          Profile.java    -> ProfileRequest.java
#    - Kế thừa (extends): Nếu Entity có extends class khác (VD: BaseEntity),
#      script sẽ kiểm tra xem class cha (BaseRequest) đã tồn tại ở thư mục 
#      đầu ra chưa. Nếu chưa, tự động tạo một skeleton class cha.
#    - Lọc field: Tự động bỏ qua các field đã thuộc về class cha 
#      (isActive, createdAt, updatedAt) để tránh trùng lặp khi compile.
#    - Annotation: Loại bỏ toàn bộ @Entity, @Table, @Column, @Id, 
#      @ManyToOne... Chỉ giữ lại khai báo field và thêm @Data (Lombok).
#
#  CHỐNG GHI ĐÈ:
#    Mặc định không ghi đè file đã tồn tại.
#    Ghi đè: FORCE=1 ./03-entity-to-request.sh
# =====================================================================
set -euo pipefail

# =====================================================================
# 1. BIẾN CẤU HÌNH (ĐỔI TẠI ĐÂY NẾU CẦN)
# =====================================================================
# Package dự phòng, chỉ dùng khi đường dẫn không suy ra được package
DEFAULT_PACKAGE="org.software.open.source.audio.io.requests"
# Các field mặc định của BaseRequest -> không sinh lại trong Request con
BASE_REQUEST_COLUMNS="isActive createdAt updatedAt"

# =====================================================================
# 2. HỎI ĐƯỜNG DẪN KHI CHẠY
# =====================================================================
echo "=================================================="
echo "  JAVA ENTITY -> REQUEST GENERATOR"
echo "=================================================="

# ---- 2.1. Hỏi file Entity đầu vào (hỏi lại tới khi hợp lệ) ----
IN_FILE=""
while [ ! -f "${IN_FILE}" ]; do
    read -r -p "Nhap duong dan tuyet doi cua file Java Entity dau vao: " IN_FILE
    if [ ! -f "${IN_FILE}" ]; then
        echo "  !! File khong ton tai: ${IN_FILE}"
    fi
done

# ---- 2.2. Hỏi thư mục Request đầu ra ----
OUT_DIR=""
while [ -z "${OUT_DIR}" ]; do
    read -r -p "Nhap duong dan tuyet doi thu muc Request dau ra: " OUT_DIR
    if [ -z "${OUT_DIR}" ]; then
        echo "  !! Duong dan khong duoc de trong"
    fi
done

# Bỏ slash cuối rồi tạo thư mục nếu chưa có
OUT_DIR="${OUT_DIR%/}"
mkdir -p "${OUT_DIR}"

# =====================================================================
# 3. SUY RA PACKAGE TỪ CHÍNH THƯ MỤC ĐẦU RA
#    /.../src/main/java/org/software/.../requests
#    -> org.software...requests
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

# Hàm ghi file (chống ghi đè)
write_file() {
    local path="$1"
    if [ -e "${path}" ] && [ "${FORCE:-0}" != "1" ]; then
        echo "  SKIP (da ton tai): ${path}"
        return
    fi
    cat > "${path}"
    echo "  CREATED: ${path}"
}

# Hàm chuẩn hóa tên class: Bỏ "Entity", thêm "Request"
to_request_name() {
    local name="$1"
    if [[ "$name" == *Entity ]]; then
        echo "${name%Entity}Request"
    else
        echo "${name}Request"
    fi
}

# =====================================================================
# 5. PARSE THÔNG TIN TỪ FILE ENTITY
# =====================================================================
echo "Dang phan tich file: ${IN_FILE}"

# Lấy thông tin class và class cha (extends)
CLASS_LINE=$(grep -E 'public\s+class\s+' "${IN_FILE}" | head -1 || true)
ORIG_CLASS=""
PARENT_CLASS=""

if [[ "${CLASS_LINE}" =~ public[[:space:]]+class[[:space:]]+([A-Za-z0-9_]+)([[:space:]]+extends[[:space:]]+([A-Za-z0-9_]+))? ]]; then
    ORIG_CLASS="${BASH_REMATCH[1]}"
    PARENT_CLASS="${BASH_REMATCH[3]}"
else
    echo "!! Khong tim thay dinh nghia class hop le trong file."
    exit 1
fi

# Xác định tên class đầu ra
OUT_CLASS=$(to_request_name "${ORIG_CLASS}")
OUT_FILE="${OUT_DIR}/${OUT_CLASS}.java"

# =====================================================================
# 6. XỬ LÝ CLASS CHA (EXTENDS)
# =====================================================================
EXTENDS_CLAUSE=""
if [ -n "${PARENT_CLASS}" ]; then
    PARENT_OUT_CLASS=$(to_request_name "${PARENT_CLASS}")
    PARENT_FILE="${OUT_DIR}/${PARENT_OUT_CLASS}.java"
    
    echo "Kiem tra class cha: ${PARENT_OUT_CLASS}"
    if [ ! -f "${PARENT_FILE}" ]; then
        echo "  -> Chua ton tai, dang tao skeleton cho class cha..."
        write_file "${PARENT_FILE}" <<EOF
package ${PACKAGE};

import lombok.Data;

/**
 * Base Request class generated from ${PARENT_CLASS}
 */
@Data
public class ${PARENT_OUT_CLASS} {
    // TODO: Them cac fields co ban cua class cha vao day neu can
}
EOF
    else
        echo "  -> Da ton tai: ${PARENT_FILE}"
    fi
    EXTENDS_CLAUSE="extends ${PARENT_OUT_CLASS} "
fi

# =====================================================================
# 7. TRÍCH XUẤT VÀ LỌC FIELD
# =====================================================================
# Lấy các dòng khai báo private, bỏ qua static, bỏ qua annotation
# Format output: Type|name
RAW_FIELDS=$(grep -E '^\s*private\s+' "${IN_FILE}" | grep -v 'static' | sed -E 's/^\s*private\s+([a-zA-Z0-9_<>]+)\s+([a-zA-Z0-9_]+);.*/\1|\2/' || true)

FIELDS=""
NEEDS_LIST=false
NEEDS_SET=false
NEEDS_MAP=false
NEEDS_INSTANT=false
NEEDS_LOCALDATE=false
NEEDS_LOCALDATETIME=false
NEEDS_BIGDECIMAL=false
NEEDS_UUID=false

while IFS='|' read -r ftype fname; do
    [ -z "${ftype}" ] || [ -z "${fname}" ] && continue
    
    # Lọc bỏ field nếu đã thuộc về BaseRequest (khi có extends)
    if [ -n "${PARENT_CLASS}" ]; then
        case " ${BASE_REQUEST_COLUMNS} " in
            *" ${fname} "*) continue ;;
        esac
    fi

    # Build field string
    FIELDS+="    private ${ftype} ${fname};"$'\n'

    # Check imports based on type
    case "${ftype}" in
        *"List<"*)  NEEDS_LIST=true ;;
        *"Set<"*)   NEEDS_SET=true ;;
        *"Map<"*)   NEEDS_MAP=true ;;
        *"Instant"*) NEEDS_INSTANT=true ;;
        *"LocalDate"*) NEEDS_LOCALDATE=true ;;
        *"LocalDateTime"*) NEEDS_LOCALDATETIME=true ;;
        *"BigDecimal"*) NEEDS_BIGDECIMAL=true ;;
        *"UUID"*)   NEEDS_UUID=true ;;
    esac
done <<< "${RAW_FIELDS}"

# =====================================================================
# 8. XÂY DỰNG IMPORT ĐỘNG
# =====================================================================
IMPORTS="import lombok.Data;"
[ "${NEEDS_LIST}" = true ] && IMPORTS+=$'\n'"import java.util.List;"
[ "${NEEDS_SET}" = true ] && IMPORTS+=$'\n'"import java.util.Set;"
[ "${NEEDS_MAP}" = true ] && IMPORTS+=$'\n'"import java.util.Map;"
[ "${NEEDS_INSTANT}" = true ] && IMPORTS+=$'\n'"import java.time.Instant;"
[ "${NEEDS_LOCALDATE}" = true ] && IMPORTS+=$'\n'"import java.time.LocalDate;"
[ "${NEEDS_LOCALDATETIME}" = true ] && IMPORTS+=$'\n'"import java.time.LocalDateTime;"
[ "${NEEDS_BIGDECIMAL}" = true ] && IMPORTS+=$'\n'"import java.math.BigDecimal;"
[ "${NEEDS_UUID}" = true ] && IMPORTS+=$'\n'"import java.util.UUID;"

# =====================================================================
# 9. SINH FILE REQUEST
# =====================================================================
echo "Dang sinh file: ${OUT_FILE}"

write_file "${OUT_FILE}" <<EOF
package ${PACKAGE};

${IMPORTS}

/**
 * Request/DTO class generated from ${ORIG_CLASS}
 */
@Data
public class ${OUT_CLASS} ${EXTENDS_CLAUSE}{

${FIELDS}}
EOF

echo "=================================================="
echo "HOAN TAT: Sinh Request tu ${IN_FILE}"
echo "Thu muc dau ra : ${OUT_DIR}"
echo "File dau ra    : ${OUT_FILE}"
echo "=================================================="