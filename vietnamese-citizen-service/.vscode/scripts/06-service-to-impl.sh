#!/usr/bin/env bash
# =====================================================================
#  06-service-to-impl.sh
# ---------------------------------------------------------------------
#  MỤC ĐÍCH:
#    Đọc một file Java Service interface, tự động sinh file Java
#    ServiceImpl tương ứng (implements interface, thêm annotation).
#
#  CÁCH DÙNG (KHÔNG truyền argument):
#    ./06-service-to-impl.sh
#    Script sẽ HỎI LẦN LƯỢT khi chạy:
#      1) Đường dẫn tuyệt đối của file Java Service interface đầu vào
#      2) Đường dẫn tuyệt đối của thư mục ServiceImpl đầu ra
#
#  PACKAGE:
#    KHÔNG hỏi package riêng. Package được SUY RA TỪ THƯ MỤC ĐẦU RA:
#      /xxx/src/main/java/org/software/open/source/.../service/impl
#      -> org.software.open.source...service.impl
#    Nếu đường dẫn không chứa /src/main/java/ thì mới hỏi package thủ công.
#
#  QUY TẮC SINH:
#    - Tên class: Thêm "Impl" vào cuối tên interface.
#      VD: BookService.java -> BookServiceImpl.java
#          UserService.java -> UserServiceImpl.java
#    - Annotation: @Service, @Slf4j, @RequiredArgsConstructor
#    - Implements: implements <InterfaceName>
#    - Body: rỗng, để developer tự thêm method.
#
#  CHỐNG GHI ĐÈ:
#    Mặc định không ghi đè file đã tồn tại.
#    Ghi đè: FORCE=1 ./06-service-to-impl.sh
# =====================================================================
set -euo pipefail

# =====================================================================
# 1. BIẾN CẤU HÌNH (ĐỔI TẠI ĐÂY NẾU CẦN)
# =====================================================================
# Package dự phòng, chỉ dùng khi đường dẫn không suy ra được package
DEFAULT_PACKAGE="org.software.open.source.audio.service.impl"

# =====================================================================
# 2. HỎI ĐƯỜNG DẪN KHI CHẠY
# =====================================================================
echo "=================================================="
echo "  JAVA SERVICE INTERFACE -> IMPLEMENTATION GENERATOR"
echo "=================================================="

# ---- 2.1. Hỏi file Service interface đầu vào (hỏi lại tới khi hợp lệ) ----
IN_FILE=""
while [ ! -f "${IN_FILE}" ]; do
    read -r -p "Nhap duong dan tuyet doi cua file Java Service interface dau vao: " IN_FILE
    if [ ! -f "${IN_FILE}" ]; then
        echo "  !! File khong ton tai: ${IN_FILE}"
    fi
done

# ---- 2.2. Hỏi thư mục ServiceImpl đầu ra ----
OUT_DIR=""
while [ -z "${OUT_DIR}" ]; do
    read -r -p "Nhap duong dan tuyet doi thu muc ServiceImpl dau ra: " OUT_DIR
    if [ -z "${OUT_DIR}" ]; then
        echo "  !! Duong dan khong duoc de trong"
    fi
done

# Bỏ slash cuối rồi tạo thư mục nếu chưa có
OUT_DIR="${OUT_DIR%/}"
mkdir -p "${OUT_DIR}"

# =====================================================================
# 3. SUY RA PACKAGE TỪ CHÍNH THƯ MỤC ĐẦU RA
#    /.../src/main/java/org/software/.../service/impl
#    -> org.software...service.impl
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

# Hàm chuẩn hóa tên: Service -> ServiceImpl
to_impl_name() {
    local name="$1"
    if [[ "$name" == *Service ]]; then
        echo "${name}Impl"
    else
        echo "${name}Impl"
    fi
}

# =====================================================================
# 5. PARSE THÔNG TIN TỪ FILE SERVICE INTERFACE
# =====================================================================
echo "Dang phan tich file: ${IN_FILE}"

# Lấy package TỪ FILE INTERFACE (để import)
INTERFACE_PACKAGE=$(grep -E '^package\s+' "${IN_FILE}" | head -1 | sed -E 's/^package\s+([^;]+);.*/\1/' || true)
if [ -z "${INTERFACE_PACKAGE}" ]; then
    echo "!! Khong tim thay package trong file interface."
    exit 1
fi

# Lấy tên interface
INTERFACE_LINE=$(grep -E 'public\s+interface\s+' "${IN_FILE}" | head -1 || true)
INTERFACE_NAME=""
if [[ "${INTERFACE_LINE}" =~ public[[:space:]]+interface[[:space:]]+([A-Za-z0-9_]+) ]]; then
    INTERFACE_NAME="${BASH_REMATCH[1]}"
else
    echo "!! Khong tim thay dinh nghia interface hop le trong file."
    exit 1
fi

# Xác định tên class implement
IMPL_CLASS=$(to_impl_name "${INTERFACE_NAME}")
IMPL_FILE="${OUT_DIR}/${IMPL_CLASS}.java"

echo "  Interface: ${INTERFACE_NAME}"
echo "  Implement: ${IMPL_CLASS}"

# =====================================================================
# 6. SINH FILE SERVICE IMPLEMENTATION
# =====================================================================
echo "Dang sinh file: ${IMPL_FILE}"

write_file "${IMPL_FILE}" <<EOF
package ${PACKAGE};

import ${INTERFACE_PACKAGE}.${INTERFACE_NAME};
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ${IMPL_CLASS} implements ${INTERFACE_NAME} {
}
EOF

echo "=================================================="
echo "HOAN TAT: Sinh ServiceImpl tu ${IN_FILE}"
echo "Thu muc dau ra : ${OUT_DIR}"
echo "File dau ra    : ${IMPL_FILE}"
echo "=================================================="