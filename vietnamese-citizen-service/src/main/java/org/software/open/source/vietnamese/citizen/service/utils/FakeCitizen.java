package org.software.open.source.vietnamese.citizen.service.utils;

import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("java:S1192")
public final class FakeCitizen {

  // Private constructor để ngăn chặn việc khởi tạo instance (Utility class
  // pattern)
  private FakeCitizen() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  // 100 FIRST_NAMES - Kết hợp tiếng Việt và tiếng Anh (nam & nữ)
  private static final String[] FIRST_NAMES = {
      // Tên tiếng Việt phổ biến
      "An", "Anh", "Ánh", "Bách", "Bình", "Cường", "Dũng", "Đạt", "Đức", "Duy",
      "Giang", "Hà", "Hải", "Hân", "Hằng", "Hòa", "Hùng", "Huy", "Huyền", "Khánh",
      "Khoa", "Khôi", "Lan", "Linh", "Long", "Mạnh", "Minh", "My", "Nam", "Nga",
      "Ngân", "Nghi", "Ngọc", "Nhi", "Như", "Oanh", "Phong", "Phúc", "Phương", "Quân",
      "Quang", "Quỳnh", "Sơn", "Tâm", "Tân", "Thảo", "Thành", "Thiên", "Thịnh", "Thu",
      "Thùy", "Thương", "Tiến", "Trâm", "Trang", "Trí", "Trung", "Trương", "Tú", "Tùng",
      "Vân", "Việt", "Vinh", "Xuân", "Yến", "Bảo", "Châu", "Đan", "Đông", "Gia",
      "Hạnh", "Hậu", "Hoài", "Hoàng", "Hồng", "Kiên", "Kim", "Lâm", "Liên", "Mai",
      "Nghĩa", "Nhật", "Phát", "Phú", "Quốc", "Sáng", "Thanh", "Thi", "Thủy", "Tiên",
      // Tên tiếng Anh phổ biến
      "James", "John", "Robert", "Michael", "William", "David", "Richard", "Joseph", "Thomas", "Charles"
  };

  // 500 LAST_NAMES - Kết hợp họ tiếng Việt và tiếng Anh
  private static final String[] LAST_NAMES = {
      // Họ tiếng Việt phổ biến (theo tỷ lệ dân số)
      "Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ", "Võ", "Đặng",
      "Bùi", "Đỗ", "Ngô", "Hồ", "Dương", "Lý", "Trương", "Đào", "Đinh", "Lương",
      "Vương", "Trịnh", "Cao", "Tạ", "Tôn", "Hà", "Tô", "Thái", "La", "Hứa",
      "Quách", "Tăng", "Đoàn", "Triệu", "Tống", "Quang", "Sầm", "Sử", "Tất", "Tiết",
      "Tiêu", "Thiếu", "Thôi", "Thủy", "Thù", "Thường", "Tiền", "Tào", "Tân", "Tần",
      "Trần", "Trang", "Trì", "Trịnh", "Trương", "Trà", "Trác", "Trạch", "Trảm", "Trang",
      "Từ", "Tư", "Uông", "Ứng", "Văn", "Vĩnh", "Vũ", "Võ", "Vương", "Xà",
      "Ximen", "Xuân", "Yên", "Âu", "Ái", "Bạch", "Bàn", "Bảo", "Bắc", "Bành",
      "Biên", "Biện", "Bính", "Bùi", "Bửu", "Cầm", "Cao", "Cát", "Cảnh", "Cao",
      "Châu", "Chiêm", "Chu", "Chung", "Chử", "Cổ", "Cung", "Cung", "Cường", "Danh",
      "Dân", "Diệu", "Doãn", "Đái", "Đàm", "Đào", "Đậu", "Đặng", "Điền", "Đinh",
      "Đoàn", "Đồng", "Đỗ", "Đường", "Dư", "Dũng", "Dương", "Dzu", "Giang", "Giáp",
      "Hà", "Hạ", "Hải", "Hàn", "Hồ", "Hòa", "Hoàng", "Huỳnh", "Hứa", "Hướng",
      "Hy", "Kha", "Khâu", "Khuất", "Kiều", "Kim", "Kỳ", "La", "Lạc", "Lâm",
      "Lân", "Lăng", "Lãnh", "Lào", "Lê", "Liễu", "Liên", "Linh", "Lưu", "Lương",
      "Lý", "Mã", "Mạch", "Mai", "Mạnh", "Mao", "Mẫn", "Miêu", "Minh", "Mông",
      "Nghiêm", "Ngô", "Nguyễn", "Nghê", "Nghiêm", "Ngư", "Nhâm", "Nhan", "Nhiêu", "Nhung",
      "Ninh", "Nông", "Phan", "Phạm", "Phùng", "Phí", "Phó", "Phong", "Phúc", "Phùng",
      "Quách", "Quan", "Quản", "Quang", "Quảng", "Quế", "Quyền", "Sài", "Sầm", "Sử",
      "Tạ", "Tào", "Tăng", "Tân", "Tần", "Tất", "Thái", "Thang", "Thành", "Thảo",
      "Thi", "Thiên", "Thiệu", "Thôi", "Thủy", "Thù", "Thường", "Tiền", "Tiết", "Tiêu",
      "Tô", "Tôn", "Tống", "Trần", "Trang", "Trì", "Trịnh", "Trương", "Trà", "Trác",
      "Trạch", "Trảm", "Trang", "Từ", "Tư", "Uông", "Ứng", "Văn", "Vĩnh", "Vũ",
      "Võ", "Vương", "Xà", "Ximen", "Xuân", "Yên", "Âu", "Ái", "Bạch", "Bàn",
      "Bảo", "Bắc", "Bành", "Biên", "Biện", "Bính", "Bùi", "Bửu", "Cầm", "Cao",
      "Cát", "Cảnh", "Châu", "Chiêm", "Chu", "Chung", "Chử", "Cổ", "Cung", "Cường",
      "Danh", "Dân", "Diệu", "Doãn", "Đái", "Đàm", "Đào", "Đậu", "Đặng", "Điền",
      "Đinh", "Đoàn", "Đồng", "Đỗ", "Đường", "Dư", "Dũng", "Dương", "Dzu", "Giang",
      "Giáp", "Hà", "Hạ", "Hải", "Hàn", "Hồ", "Hòa", "Hoàng", "Huỳnh", "Hứa",
      "Hướng", "Hy", "Kha", "Khâu", "Khuất", "Kiều", "Kim", "Kỳ", "La", "Lạc",
      "Lâm", "Lân", "Lăng", "Lãnh", "Lào", "Lê", "Liễu", "Liên", "Linh", "Lưu",
      "Lương", "Lý", "Mã", "Mạch", "Mai", "Mạnh", "Mao", "Mẫn", "Miêu", "Minh",
      "Mông", "Nghiêm", "Ngô", "Nguyễn", "Nghê", "Ngư", "Nhâm", "Nhan", "Nhiêu", "Nhung",
      "Ninh", "Nông", "Phan", "Phạm", "Phùng", "Phí", "Phó", "Phong", "Phúc", "Phùng",
      // Họ tiếng Anh phổ biến
      "Smith", "Jones", "Williams", "Brown", "Taylor", "Davis", "Wilson", "Evans", "Thomas", "Johnson",
      "Roberts", "Walker", "Wright", "Robinson", "Thompson", "White", "Hughes", "Edwards", "Green", "Hall",
      "Lewis", "Harris", "Clarke", "Patel", "Jackson", "Wood", "Turner", "Martin", "Cooper", "Hill",
      "Ward", "Morris", "Moore", "Clark", "King", "Baker", "Harrison", "Morgan", "Allen", "James",
      "Scott", "Phillips", "Watson", "Davies", "Wilson", "Carter", "Collins", "Bell", "Stewart", "Murphy",
      "Bailey", "Cook", "Rogers", "Gray", "Hunt", "Henderson", "Perry", "Powell", "Ross", "Jenkins",
      "Long", "Patterson", "Hughes", "Flores", "Washington", "Butler", "Simmons", "Foster", "Gonzales", "Bryant",
      "Alexander", "Russell", "Griffin", "Diaz", "Hayes", "Myers", "Ford", "Hamilton", "Graham", "Sullivan",
      "Wallace", "Woods", "Cole", "West", "Jordan", "Owens", "Reynolds", "Fisher", "Ellis", "Harrison",
      "Gibson", "McDonald", "Cruz", "Marshall", "Ortiz", "Gomez", "Murray", "Freeman", "Wells", "Webb",
      "Simpson", "Tucker", "Porter", "Hunter", "Hicks", "Crawford", "Henry", "Boyd", "Mason", "Morales",
      "Kennedy", "Warren", "Dixon", "Ramos", "Reyes", "Burns", "Gordon", "Shaw", "Holmes", "Rice",
      "Robertson", "Hunt", "Black", "Daniels", "Palmer", "Mills", "Nichols", "Grant", "Knight", "Ferguson",
      "Rose", "Stone", "Hawkins", "Dunn", "Perkins", "Hudson", "Spencer", "Gardner", "Stephens", "Payne",
      "Pierce", "Berry", "Matthews", "Arnold", "Wagner", "Willis", "Ray", "Watkins", "Olson", "Carroll",
      "Duncan", "Snyder", "Hart", "Cunningham", "Bradley", "Lane", "Andrews", "Ruiz", "Harper", "Fox",
      "Riley", "Armstrong", "Carpenter", "Weaver", "Greene", "Lawrence", "Elliott", "Chavez", "Sims", "Austin",
      "Peters", "Kelley", "Franklin", "Lawson", "Fields", "Gutierrez", "Ryan", "Schmidt", "Carr", "Vasquez",
      "Castillo", "Wheeler", "Chapman", "Oliver", "Montgomery", "Richards", "Williamson", "Johnston", "Banks", "Meyer",
      "Bishop", "McCoy", "Howell", "Alvarez", "Morrison", "Hansen", "Fernandez", "Garza", "Harvey", "Little",
      "Burton", "Stanley", "Nguyen", "George", "Jacobs", "Reid", "Kim", "Fuller", "Lynch", "Dean",
      "Gilbert", "Garrett", "Romero", "Welch", "Larson", "Frazier", "Burke", "Hanson", "Day", "Mendoza",
      "Moreno", "Bowman", "Medina", "Fowler", "Brewer", "Hoffman", "Carlson", "Silva", "Pearson", "Holland",
      "Douglas", "Fleming", "Jensen", "Vargas", "Byrd", "Davidson", "Hopkins", "May", "Terry", "Herrera",
      "Sloan", "Pacheco", "Wolfe", "Padilla", "Warner", "O'Brien", "Acosta", "Robles", "Clements", "Soto",
      "Gross", "Fitzgerald", "Molloy", "Boyle", "McMahon", "O'Connor", "O'Neill", "O'Sullivan", "McLaughlin", "Cohen",
      "Miller", "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia", "Martinez",
      "Robinson", "Clark", "Rodriguez", "Lewis", "Lee", "Walker", "Hall", "Allen", "Young", "King",
      "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores", "Green", "Adams", "Nelson", "Baker",
      "Hall", "Rivera", "Campbell", "Mitchell", "Carter", "Roberts", "Gomez", "Phillips", "Evans", "Turner",
      "Diaz", "Parker", "Edwards", "Collins", "Stewart", "Sanchez", "Morris", "Rogers", "Reed", "Cook",
      "Morgan", "Bell", "Murphy", "Bailey", "Rivera", "Cooper", "Richardson", "Cox", "Howard", "Ward",
      "Torres", "Peterson", "Gray", "Ramirez", "James", "Watson", "Brooks", "Kelly", "Sanders", "Price",
      "Bennett", "Wood", "Barnes", "Ross", "Henderson", "Coleman", "Jenkins", "Perry", "Powell", "Long",
      "Patterson", "Hughes", "Flores", "Washington", "Butler", "Simmons", "Foster", "Gonzales", "Bryant", "Alexander",
      "Russell", "Griffin", "Diaz", "Hayes", "Myers", "Ford", "Hamilton", "Graham", "Sullivan", "Wallace"
  };

  /**
   * Trả về một First Name ngẫu nhiên.
   */
  public static String getFirstName() {
    return FIRST_NAMES[ThreadLocalRandom.current().nextInt(FIRST_NAMES.length)];
  }

  /**
   * Trả về một Last Name ngẫu nhiên.
   */
  public static String getLastName() {
    return LAST_NAMES[ThreadLocalRandom.current().nextInt(LAST_NAMES.length)];
  }

  /**
   * Trả về Full Name ngẫu nhiên.
   * Ghép theo chuẩn tiếng Việt: Họ + Tên (Ví dụ: Nguyễn An)
   * Nếu anh muốn chuẩn tiếng Anh (First + Last), anh có thể đổi ngược lại.
   */
  public static String getFullName() {
    return getLastName() + " " + getFirstName();
  }
}
