#!/usr/bin/env bash
# =====================================================================
#  04-entity-to-repository.sh
# ---------------------------------------------------------------------
#  MỤC ĐÍCH:
#    Đọc một file Java JPA Entity, tự động sinh file Java Repository
#    interface tương ứng (extends JpaRepository).
#
#  CÁCH DÙNG (KHÔNG truyền argument):
#    ./04-entity-to-repository.sh
#    Script sẽ HỎI LẦN LƯỢT khi chạy:
#      1) Đường dẫn tuyệt đối của file Java Entity đầu vào
#      2) Đường dẫn tuyệt đối của thư mục Repository đầu ra
#
#  PACKAGE:
#    KHÔNG hỏi package riêng. Package được SUY RA TỪ THƯ MỤC ĐẦU RA:
#      /xxx/src/main/java/org/software/open/source/.../repositories
#      -> org.software.open.source...repositories
#    Nếu đường dẫn không chứa /src/main/java/ thì mới hỏi package thủ công.
#
#  QUY TẮC SINH:
#    - Tên interface: Thêm "Repository" vào cuối tên Entity.
#      VD: BookEntity.java -> BookRepository.java
#          UserEntity.java -> UserRepository.java
#    - Extends: JpaRepository<EntityType, IdType>
#    - ID Type: Tự động phát hiện kiểu dữ liệu của field @Id trong Entity
#      (Long, Integer, UUID, String, ...)
#    - KHÔNG có custom query methods - chỉ interface cơ bản.
#
#  CHỐNG GHI ĐÈ:
#    Mặc định không ghi đè file đã tồn tại.
#    Ghi đè: FORCE=1 ./04-entity-to-repository.sh
# =====================================================================
set -euo pipefail

# =====================================================================
# 1. BIẾN CẤU HÌNH (ĐỔI TẠI ĐÂY NẾU CẦN)
# =====================================================================
# Package dự phòng, chỉ dùng khi đường dẫn không suy ra được package
DEFAULT_PACKAGE="org.software.open.source.audio.io.repositories"

# =====================================================================
# 2. HỎI ĐƯỜNG DẪN KHI CHẠY
# =====================================================================
echo "=================================================="
echo "  JAVA ENTITY -> REPOSITORY GENERATOR"
echo "=================================================="

# ---- 2.1. Hỏi file Entity đầu vào (hỏi lại tới khi hợp lệ) ----
IN_FILE=""
while [ ! -f "${IN_FILE}" ]; do
    read -r -p "Nhap duong dan tuyet doi cua file Java Entity dau vao: " IN_FILE
    if [ ! -f "${IN_FILE}" ]; then
        echo "  !! File khong ton tai: ${IN_FILE}"
    fi
done

# ---- 2.2. Hỏi thư mục Repository đầu ra ----
OUT_DIR=""
while [ -z "${OUT_DIR}" ]; do
    read -r -p "Nhap duong dan tuyet doi thu muc Repository dau ra: " OUT_DIR
    if [ -z "${OUT_DIR}" ]; then
        echo "  !! Duong dan khong duoc de trong"
    fi
done

# Bỏ slash cuối rồi tạo thư mục nếu chưa có
OUT_DIR="${OUT_DIR%/}"
mkdir -p "${OUT_DIR}"

# =====================================================================
# 3. SUY RA PACKAGE TỪ CHÍNH THƯ MỤC ĐẦU RA
#    /.../src/main/java/org/software/.../repositories
#    -> org.software...repositories
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

# Hàm chuẩn hóa tên: Entity -> Repository
to_repository_name() {
    local name="$1"
    if [[ "$name" == *Entity ]]; then
        echo "${name%Entity}Repository"
    else
        echo "${name}Repository"
    fi
}

# =====================================================================
# 5. PARSE THÔNG TIN TỪ FILE ENTITY
# =====================================================================
echo "Dang phan tich file: ${IN_FILE}"

# Lấy package TỪ FILE ENTITY (để import)
ENTITY_PACKAGE=$(grep -E '^package\s+' "${IN_FILE}" | head -1 | sed -E 's/^package\s+([^;]+);.*/\1/' || true)
if [ -z "${ENTITY_PACKAGE}" ]; then
    echo "!! Khong tim thay package trong file Entity."
    exit 1
fi

# Lấy tên class Entity
CLASS_LINE=$(grep -E 'public\s+(abstract\s+)?class\s+' "${IN_FILE}" | head -1 || true)
ORIG_CLASS=""
if [[ "${CLASS_LINE}" =~ public[[:space:]]+(abstract[[:space:]]+)?class[[:space:]]+([A-Za-z0-9_]+) ]]; then
    ORIG_CLASS="${BASH_REMATCH[2]}"
else
    echo "!! Khong tim thay dinh nghia class hop le trong file."
    exit 1
fi

# Xác định tên Repository
REPO_CLASS=$(to_repository_name "${ORIG_CLASS}")
REPO_FILE="${OUT_DIR}/${REPO_CLASS}.java"

# =====================================================================
# 6. TÌM KIỂU DỮ LIỆU CỦA @Id
# =====================================================================
ID_TYPE=""

# Tìm dòng có @Id và extract kiểu dữ liệu
# Có thể @Id nằm trên dòng riêng hoặc cùng dòng với field
while IFS= read -r line; do
    [ -z "${line}" ] && continue
    
    # Nếu dòng chứa @Id
    if [[ "${line}" =~ @Id ]]; then
        # Kiểm tra xem dòng tiếp theo có khai báo field không
        # Hoặc dòng hiện tại có field không
        if [[ "${line}" =~ private[[:space:]]+([A-Za-z0-9_<>]+)[[:space:]]+([a-zA-Z0-9_]+) ]]; then
            ID_TYPE="${BASH_REMATCH[1]}"
            break
        fi
    fi
done < "${IN_FILE}"

# Nếu chưa tìm thấy, thử cách khác: tìm @Id và dòng private kế tiếp
if [ -z "${ID_TYPE}" ]; then
    found_id=false
    while IFS= read -r line; do
        [ -z "${line}" ] && continue
        
        if [[ "${line}" =~ @Id ]]; then
            found_id=true
            continue
        fi
        
        if [ "${found_id}" = true ]; then
            if [[ "${line}" =~ private[[:space:]]+([A-Za-z0-9_<>]+)[[:space:]]+([a-zA-Z0-9_]+) ]]; then
                ID_TYPE="${BASH_REMATCH[1]}"
                break
            fi
            found_id=false
        fi
    done < "${IN_FILE}"
fi

# Default to Long if not found
if [ -z "${ID_TYPE}" ]; then
    echo "  ! Khong tim thay @Id, su dung Long lam mac dinh"
    ID_TYPE="Long"
fi

echo "  ID Type: ${ID_TYPE}"

# =====================================================================
# 7. XÁC ĐỊNH IMPORT CẦN THIẾT
# =====================================================================
# Import cho ID type nếu cần
ID_IMPORT=""
case "${ID_TYPE}" in
    UUID)
        ID_IMPORT="import java.util.UUID;"
        ;;
    BigDecimal)
        ID_IMPORT="import java.math.BigDecimal;"
        ;;
    Instant|LocalDate|LocalDateTime)
        ID_IMPORT="import java.time.${ID_TYPE};"
        ;;
esac

# =====================================================================
# 8. SINH FILE REPOSITORY
# =====================================================================
echo "Dang sinh file: ${REPO_FILE}"

write_file "${REPO_FILE}" <<EOF
package ${PACKAGE};

import ${ENTITY_PACKAGE}.${ORIG_CLASS};
import org.springframework.data.jpa.repository.JpaRepository;
${ID_IMPORT:+${ID_IMPORT}
}
/**
 * Repository interface for ${ORIG_CLASS}
 * Generated by 04-entity-to-repository.sh
 */
public interface ${REPO_CLASS} extends JpaRepository<${ORIG_CLASS}, ${ID_TYPE}> {
}
EOF

echo "=================================================="
echo "HOAN TAT: Sinh Repository tu ${IN_FILE}"
echo "Thu muc dau ra : ${OUT_DIR}"
echo "File dau ra    : ${REPO_FILE}"
echo "=================================================="