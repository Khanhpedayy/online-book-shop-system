/* =========================================================
   SEED DATA — 100 BOOKS (diverse categories)
   Run AFTER databasebansach.sql + roles/users seeded
   ========================================================= */

SET IDENTITY_INSERT categories ON;

INSERT INTO categories (id, name, slug, description, sort_order, is_active) VALUES
(1,  N'Văn học Việt Nam',       'van-hoc-viet-nam',       N'Tiểu thuyết, truyện ngắn, thơ Việt Nam',       1, 1),
(2,  N'Văn học nước ngoài',     'van-hoc-nuoc-ngoai',     N'Tiểu thuyết, truyện dịch từ nước ngoài',       2, 1),
(3,  N'Kinh tế - Kinh doanh',  'kinh-te-kinh-doanh',     N'Quản trị, marketing, tài chính, khởi nghiệp',  3, 1),
(4,  N'Kỹ năng sống',          'ky-nang-song',           N'Phát triển bản thân, giao tiếp, tư duy',        4, 1),
(5,  N'Khoa học - Công nghệ',  'khoa-hoc-cong-nghe',     N'Khoa học tự nhiên, IT, lập trình',              5, 1),
(6,  N'Thiếu nhi',             'thieu-nhi',              N'Truyện tranh, sách giáo dục cho trẻ',           6, 1),
(7,  N'Tâm lý - Triết học',    'tam-ly-triet-hoc',       N'Tâm lý học, triết học, tâm linh',               7, 1),
(8,  N'Lịch sử - Địa lý',     'lich-su-dia-ly',         N'Lịch sử Việt Nam và thế giới',                  8, 1),
(9,  N'Manga - Comic',         'manga-comic',            N'Manga Nhật Bản, comic phương Tây',              9, 1),
(10, N'Sách giáo khoa - Tham khảo', 'sach-giao-khoa',    N'SGK, sách luyện thi, tài liệu học tập',        10, 1);

SET IDENTITY_INSERT categories OFF;

-- ===================== AUTHORS =====================

SET IDENTITY_INSERT authors ON;

INSERT INTO authors (id, name, slug, bio) VALUES
(1,  N'Nguyễn Nhật Ánh',      'nguyen-nhat-anh',      N'Nhà văn nổi tiếng với các tác phẩm về tuổi thơ'),
(2,  N'Tô Hoài',              'to-hoai',              N'Tác giả Dế Mèn phiêu lưu ký'),
(3,  N'Nam Cao',              'nam-cao',              N'Nhà văn hiện thực phê phán'),
(4,  N'Haruki Murakami',      'haruki-murakami',      N'Tiểu thuyết gia Nhật Bản nổi tiếng thế giới'),
(5,  N'Paulo Coelho',         'paulo-coelho',         N'Tác giả người Brazil của Nhà giả kim'),
(6,  N'Dale Carnegie',        'dale-carnegie',        N'Tác giả sách phát triển bản thân'),
(7,  N'Robert Kiyosaki',      'robert-kiyosaki',      N'Tác giả Rich Dad Poor Dad'),
(8,  N'Yuval Noah Harari',    'yuval-noah-harari',    N'Sử gia, tác giả Sapiens'),
(9,  N'Stephen Hawking',      'stephen-hawking',      N'Nhà vật lý lý thuyết'),
(10, N'J.K. Rowling',         'jk-rowling',           N'Tác giả Harry Potter'),
(11, N'George Orwell',        'george-orwell',        N'Tác giả 1984 và Trại súc vật'),
(12, N'Antoine de Saint-Exupéry', 'saint-exupery',    N'Tác giả Hoàng tử bé'),
(13, N'Nguyễn Ngọc Tư',      'nguyen-ngoc-tu',       N'Nhà văn miền Tây'),
(14, N'Mark Manson',          'mark-manson',          N'Tác giả The Subtle Art'),
(15, N'Daniel Kahneman',      'daniel-kahneman',      N'Nhà tâm lý học, Nobel Kinh tế'),
(16, N'Eiichiro Oda',         'eiichiro-oda',         N'Tác giả One Piece'),
(17, N'Gosho Aoyama',         'gosho-aoyama',         N'Tác giả Conan'),
(18, N'Fujiko F. Fujio',      'fujiko-f-fujio',       N'Tác giả Doraemon'),
(19, N'Nguyễn Phong Việt',    'nguyen-phong-viet',    N'Nhà thơ trẻ Việt Nam'),
(20, N'Adam Grant',           'adam-grant',           N'Giáo sư Wharton, tác giả Think Again'),
(21, N'Walter Isaacson',      'walter-isaacson',      N'Nhà viết tiểu sử nổi tiếng'),
(22, N'Nguyễn Huy Thiệp',    'nguyen-huy-thiep',     N'Nhà văn đương đại Việt Nam'),
(23, N'Lê Minh Khuê',        'le-minh-khue',         N'Nhà văn nữ Việt Nam'),
(24, N'Sun Tzu',              'sun-tzu',              N'Tác giả Binh pháp Tôn Tử'),
(25, N'Napoleon Hill',        'napoleon-hill',        N'Tác giả Think and Grow Rich');

SET IDENTITY_INSERT authors OFF;

-- ===================== BOOKS (100 cuốn) =====================

SET IDENTITY_INSERT books ON;

INSERT INTO books (id, category_id, title, slug, publisher_name, publication_year, language, short_description, status) VALUES
-- Văn học Việt Nam (1-12)
(1,  1, N'Tôi thấy hoa vàng trên cỏ xanh',        'toi-thay-hoa-vang-tren-co-xanh',        N'NXB Trẻ', 2018, 'vi', N'Câu chuyện tuổi thơ miền quê', 'ACTIVE'),
(2,  1, N'Mắt biếc',                                'mat-biec',                                N'NXB Trẻ', 2019, 'vi', N'Tình yêu tuổi học trò trong sáng', 'ACTIVE'),
(3,  1, N'Cho tôi xin một vé đi tuổi thơ',          'cho-toi-xin-mot-ve-di-tuoi-tho',          N'NXB Trẻ', 2020, 'vi', N'Hồi ức tuổi thơ đẹp đẽ', 'ACTIVE'),
(4,  1, N'Dế mèn phiêu lưu ký',                     'de-men-phieu-luu-ky',                     N'NXB Kim Đồng', 2015, 'vi', N'Cuộc phiêu lưu của chú Dế Mèn', 'ACTIVE'),
(5,  1, N'Chí Phèo',                                'chi-pheo',                                N'NXB Văn Học', 2016, 'vi', N'Bi kịch người nông dân bị tha hóa', 'ACTIVE'),
(6,  1, N'Cánh đồng bất tận',                       'canh-dong-bat-tan',                       N'NXB Trẻ', 2017, 'vi', N'Những mảnh đời trên cánh đồng miền Tây', 'ACTIVE'),

-- Văn học nước ngoài (13-24)
(13, 2, N'Rừng Na Uy',                              'rung-na-uy',                              N'NXB Hội Nhà Văn', 2020, 'vi', N'Tiểu thuyết nổi tiếng của Murakami', 'ACTIVE'),
(14, 2, N'Kafka bên bờ biển',                       'kafka-ben-bo-bien',                       N'NXB Hội Nhà Văn', 2019, 'vi', N'Hành trình tìm kiếm bản thể', 'ACTIVE'),
(15, 2, N'Nhà giả kim',                             'nha-gia-kim',                             N'NXB Hội Nhà Văn', 2020, 'vi', N'Câu chuyện về theo đuổi giấc mơ', 'ACTIVE'),
(16, 2, N'Hoàng tử bé',                             'hoang-tu-be',                             N'NXB Kim Đồng', 2019, 'vi', N'Tác phẩm kinh điển về tình bạn', 'ACTIVE'),
(17, 2, N'1984',                                    'mot-chin-tam-tu',                          N'NXB Hội Nhà Văn', 2021, 'vi', N'Tiểu thuyết dystopia kinh điển', 'ACTIVE'),
(18, 2, N'Trại súc vật',                            'trai-suc-vat',                            N'NXB Hội Nhà Văn', 2020, 'vi', N'Ngụ ngôn chính trị sắc sảo', 'ACTIVE'),

-- Kinh tế - Kinh doanh (25-36)
(25, 3, N'Cha giàu cha nghèo',                     'cha-giau-cha-ngheo',                     N'NXB Trẻ', 2019, 'vi', N'Bài học tài chính từ hai người cha', 'ACTIVE'),
(26, 3, N'Nghĩ giàu làm giàu',                     'nghi-giau-lam-giau',                     N'NXB Trẻ', 2018, 'vi', N'13 nguyên tắc làm giàu bất biến', 'ACTIVE'),
(27, 3, N'Dạy con làm giàu - Tập 1',               'day-con-lam-giau-tap-1',                 N'NXB Trẻ', 2020, 'vi', N'Phần tiếp theo của Cha giàu cha nghèo', 'ACTIVE'),
(28, 3, N'Từ tốt đến vĩ đại',                      'tu-tot-den-vi-dai',                      N'NXB Trẻ', 2019, 'vi', N'Good to Great - Jim Collins', 'ACTIVE'),
(29, 3, N'Khởi nghiệp tinh gọn',                   'khoi-nghiep-tinh-gon',                   N'NXB Trẻ', 2020, 'vi', N'Lean Startup methodology', 'ACTIVE'),
(30, 3, N'Tuần làm việc 4 giờ',                     'tuan-lam-viec-4-gio',                     N'NXB Lao Động', 2019, 'vi', N'Tối ưu hóa thời gian và hiệu suất', 'ACTIVE'),

-- Kỹ năng sống (37-46)
(37, 4, N'Đắc nhân tâm',                           'dac-nhan-tam',                           N'NXB Tổng Hợp', 2020, 'vi', N'Nghệ thuật thu phục lòng người', 'ACTIVE'),
(38, 4, N'Quẳng gánh lo đi và vui sống',            'quang-ganh-lo-di-va-vui-song',            N'NXB Tổng Hợp', 2019, 'vi', N'Bí quyết sống vui vẻ', 'ACTIVE'),
(39, 4, N'Nghệ thuật tinh tế của việc đếch quan tâm', 'nghe-thuat-dech-quan-tam',             N'NXB Trẻ', 2021, 'vi', N'The Subtle Art of Not Giving a F*ck', 'ACTIVE'),
(40, 4, N'Bí mật tư duy triệu phú',                'bi-mat-tu-duy-trieu-phu',                N'NXB Trẻ', 2019, 'vi', N'Secrets of the Millionaire Mind', 'ACTIVE'),
(41, 4, N'7 thói quen hiệu quả',                   '7-thoi-quen-hieu-qua',                   N'NXB Trẻ', 2020, 'vi', N'7 Habits of Highly Effective People', 'ACTIVE'),

-- Khoa học - Công nghệ (47-56)
(47, 5, N'Lược sử thời gian',                      'luoc-su-thoi-gian',                      N'NXB Trẻ', 2020, 'vi', N'A Brief History of Time', 'ACTIVE'),
(48, 5, N'Vũ trụ trong vỏ hạt dẻ',                 'vu-tru-trong-vo-hat-de',                 N'NXB Trẻ', 2019, 'vi', N'The Universe in a Nutshell', 'ACTIVE'),
(49, 5, N'Clean Code',                              'clean-code',                              N'NXB Bách Khoa', 2021, 'en', N'Hướng dẫn viết code sạch', 'ACTIVE'),
(50, 5, N'The Pragmatic Programmer',                'the-pragmatic-programmer',                N'NXB Bách Khoa', 2022, 'en', N'Lập trình viên thực dụng', 'ACTIVE'),
(51, 5, N'Giải thuật và lập trình',                 'giai-thuat-va-lap-trinh',                N'NXB Đại Học Quốc Gia', 2020, 'vi', N'Cấu trúc dữ liệu và giải thuật', 'ACTIVE')
SET IDENTITY_INSERT books OFF;

GO

-- ===================== BOOKS Part 2 (57-100) =====================

SET IDENTITY_INSERT books ON;

INSERT INTO books (id, category_id, title, slug, publisher_name, publication_year, language, short_description, status) VALUES
-- Thiếu nhi (57-66)
(57, 6, N'Dế mèn phiêu lưu ký (bìa cứng)',        'de-men-phieu-luu-ky-bia-cung',            N'NXB Kim Đồng', 2021, 'vi', N'Phiên bản bìa cứng đặc biệt', 'ACTIVE'),
(58, 6, N'Totto-chan bên cửa sổ',                   'totto-chan-ben-cua-so',                   N'NXB Kim Đồng', 2020, 'vi', N'Câu chuyện về ngôi trường đặc biệt', 'ACTIVE'),
(59, 6, N'Charlie và nhà máy Sôcôla',               'charlie-va-nha-may-socola',               N'NXB Kim Đồng', 2019, 'vi', N'Tác phẩm kinh điển Roald Dahl', 'ACTIVE'),
(60, 6, N'Pippi tất dài',                           'pippi-tat-dai',                           N'NXB Kim Đồng', 2018, 'vi', N'Cô bé mạnh mẽ nhất thế giới', 'ACTIVE'),
(61, 6, N'Kính vạn hoa - Tập 1',                    'kinh-van-hoa-tap-1',                      N'NXB Kim Đồng', 2020, 'vi', N'Bộ truyện thiếu nhi Nguyễn Nhật Ánh', 'ACTIVE'),
(62, 6, N'Truyện cổ tích Việt Nam',                 'truyen-co-tich-viet-nam',                 N'NXB Kim Đồng', 2019, 'vi', N'Tuyển tập truyện cổ tích hay nhất', 'ACTIVE'),
(63, 6, N'Hai vạn dặm dưới đáy biển',              'hai-van-dam-duoi-day-bien',              N'NXB Kim Đồng', 2020, 'vi', N'Jules Verne - Khoa học viễn tưởng', 'ACTIVE'),
(64, 6, N'Alice ở xứ sở thần tiên',                 'alice-o-xu-so-than-tien',                 N'NXB Kim Đồng', 2021, 'vi', N'Câu chuyện kỳ ảo kinh điển', 'ACTIVE'),
(65, 6, N'Truyện Kiều cho thiếu nhi',               'truyen-kieu-cho-thieu-nhi',               N'NXB Kim Đồng', 2020, 'vi', N'Phiên bản dễ hiểu cho trẻ', 'ACTIVE'),
(66, 6, N'Nhóc Maruko - Tập 1',                     'nhoc-maruko-tap-1',                       N'NXB Kim Đồng', 2019, 'vi', N'Manga thiếu nhi dễ thương', 'ACTIVE'),

-- Tâm lý - Triết học (67-76)
(67, 7, N'Tâm lý học đám đông',                    'tam-ly-hoc-dam-dong',                    N'NXB Thế Giới', 2020, 'vi', N'Gustave Le Bon - Hiểu về đám đông', 'ACTIVE'),
(68, 7, N'Ý nghĩa cuộc sống',                      'y-nghia-cuoc-song',                      N'NXB Trẻ', 2021, 'vi', N'Alfred Adler - Tâm lý học cá nhân', 'ACTIVE'),
(69, 7, N'Tội ác và hình phạt',                     'toi-ac-va-hinh-phat',                     N'NXB Văn Học', 2019, 'vi', N'Dostoevsky - Tiểu thuyết tâm lý', 'ACTIVE'),
(70, 7, N'Flow - Dòng chảy',                        'flow-dong-chay',                          N'NXB Trẻ', 2022, 'vi', N'Trạng thái tập trung tuyệt đối', 'ACTIVE'),
(71, 7, N'Triết học hiện sinh',                     'triet-hoc-hien-sinh',                     N'NXB Tri Thức', 2020, 'vi', N'Tổng quan triết học hiện sinh', 'ACTIVE'),
(72, 7, N'Tâm lý học hành vi',                     'tam-ly-hoc-hanh-vi',                     N'NXB Bách Khoa', 2021, 'vi', N'Hiểu về hành vi con người', 'ACTIVE'),
(73, 7, N'Siddhartha',                              'siddhartha',                              N'NXB Trẻ', 2020, 'vi', N'Hermann Hesse - Hành trình tâm linh', 'ACTIVE'),
(74, 7, N'Bên kia sự bận rộn',                     'ben-kia-su-ban-ron',                     N'NXB Trẻ', 2022, 'vi', N'Tìm lại sự bình yên nội tâm', 'ACTIVE'),
(75, 7, N'Đạo đức Nicomachean',                     'dao-duc-nicomachean',                     N'NXB Tri Thức', 2019, 'vi', N'Aristotle - Triết học đạo đức', 'ACTIVE'),
(76, 7, N'Người bán hàng vĩ đại nhất thế giới',    'nguoi-ban-hang-vi-dai-nhat',              N'NXB Tổng Hợp', 2020, 'vi', N'Og Mandino - Truyện truyền cảm hứng', 'ACTIVE'),

-- Lịch sử - Địa lý (77-84)
(77, 8, N'Lịch sử Việt Nam bằng tranh',            'lich-su-viet-nam-bang-tranh',            N'NXB Trẻ', 2020, 'vi', N'Bộ sách tranh lịch sử', 'ACTIVE'),
(78, 8, N'Đại Việt sử ký toàn thư',                'dai-viet-su-ky-toan-thu',                N'NXB Khoa Học Xã Hội', 2019, 'vi', N'Bộ sử kinh điển Việt Nam', 'ACTIVE'),
(79, 8, N'Guns, Germs and Steel',                   'guns-germs-and-steel',                   N'NXB Thế Giới', 2021, 'vi', N'Jared Diamond - Lịch sử văn minh', 'ACTIVE'),
(80, 8, N'Nghìn năm văn hiến',                     'nghin-nam-van-hien',                     N'NXB Giáo Dục', 2018, 'vi', N'Văn hóa Việt Nam qua các thời kỳ', 'ACTIVE'),
(81, 8, N'Steve Jobs - Tiểu sử',                    'steve-jobs-tieu-su',                      N'NXB Trẻ', 2020, 'vi', N'Walter Isaacson viết về Steve Jobs', 'ACTIVE'),
(82, 8, N'Elon Musk - Tiểu sử',                     'elon-musk-tieu-su',                       N'NXB Trẻ', 2023, 'vi', N'Walter Isaacson viết về Elon Musk', 'ACTIVE'),
(83, 8, N'Chiến tranh và hòa bình',                 'chien-tranh-va-hoa-binh',                 N'NXB Văn Học', 2020, 'vi', N'Tolstoy - Đại tác phẩm văn học', 'ACTIVE'),
(84, 8, N'Tam quốc diễn nghĩa',                    'tam-quoc-dien-nghia',                    N'NXB Văn Học', 2019, 'vi', N'La Quán Trung - Tiểu thuyết lịch sử', 'ACTIVE'),

-- Manga - Comic (85-94)
(85, 9, N'One Piece - Tập 1',                       'one-piece-tap-1',                         N'NXB Kim Đồng', 2022, 'vi', N'Manga huyền thoại về hải tặc', 'ACTIVE'),
(86, 9, N'One Piece - Tập 2',                       'one-piece-tap-2',                         N'NXB Kim Đồng', 2022, 'vi', N'Luffy tiếp tục hành trình', 'ACTIVE'),
(87, 9, N'Conan - Tập 1',                           'conan-tap-1',                             N'NXB Kim Đồng', 2021, 'vi', N'Thám tử lừng danh Conan', 'ACTIVE'),
(88, 9, N'Conan - Tập 2',                           'conan-tap-2',                             N'NXB Kim Đồng', 2021, 'vi', N'Vụ án mới của Conan', 'ACTIVE'),
(89, 9, N'Doraemon - Tập 1',                        'doraemon-tap-1',                          N'NXB Kim Đồng', 2020, 'vi', N'Chú mèo máy đến từ tương lai', 'ACTIVE'),
(90, 9, N'Doraemon - Tập 2',                        'doraemon-tap-2',                          N'NXB Kim Đồng', 2020, 'vi', N'Những bảo bối thần kỳ', 'ACTIVE'),
(91, 9, N'Dragon Ball - Tập 1',                     'dragon-ball-tap-1',                       N'NXB Kim Đồng', 2021, 'vi', N'Goku và 7 viên ngọc rồng', 'ACTIVE'),
(92, 9, N'Naruto - Tập 1',                          'naruto-tap-1',                            N'NXB Kim Đồng', 2020, 'vi', N'Ninja Naruto Uzumaki', 'ACTIVE'),
(93, 9, N'Attack on Titan - Tập 1',                 'attack-on-titan-tap-1',                   N'NXB Kim Đồng', 2022, 'vi', N'Đại chiến Titan', 'ACTIVE'),
(94, 9, N'Slam Dunk - Tập 1',                       'slam-dunk-tap-1',                         N'NXB Kim Đồng', 2021, 'vi', N'Manga bóng rổ kinh điển', 'ACTIVE'),

-- Sách giáo khoa - Tham khảo (95-100)
(95,  10, N'Toán cao cấp - Tập 1',                  'toan-cao-cap-tap-1',                     N'NXB Giáo Dục', 2022, 'vi', N'Đại số tuyến tính và giải tích', 'ACTIVE'),
(96,  10, N'Ngữ pháp tiếng Anh nâng cao',           'ngu-phap-tieng-anh-nang-cao',            N'NXB Giáo Dục', 2021, 'vi', N'English Grammar in Use', 'ACTIVE'),
(97,  10, N'IELTS Academic 18',                      'ielts-academic-18',                      N'Cambridge', 2023, 'en', N'Đề thi IELTS chính thức', 'ACTIVE'),
(98,  10, N'Vật lý đại cương',                       'vat-ly-dai-cuong',                       N'NXB Đại Học Quốc Gia', 2020, 'vi', N'Giáo trình vật lý đại học', 'ACTIVE'),
(99,  10, N'Luyện thi THPT Quốc gia - Toán',        'luyen-thi-thpt-qg-toan',                 N'NXB Giáo Dục', 2024, 'vi', N'Sách ôn thi tốt nghiệp', 'ACTIVE'),
(100, 10, N'Tiếng Nhật sơ cấp - Minna no Nihongo',  'tieng-nhat-minna-no-nihongo',            N'NXB Trẻ', 2022, 'vi', N'Giáo trình tiếng Nhật N5-N4', 'ACTIVE');

SET IDENTITY_INSERT books OFF;

GO

-- ===================== BOOK AUTHORS =====================

INSERT INTO book_authors (book_id, author_id, role, sort_order) VALUES
-- Văn học VN
(1,1,'AUTHOR',0),(2,1,'AUTHOR',0),(3,1,'AUTHOR',0),(61,1,'AUTHOR',0),
(4,2,'AUTHOR',0),(57,2,'AUTHOR',0),
(5,3,'AUTHOR',0),
(6,13,'AUTHOR',0),
-- Văn học nước ngoài
(13,4,'AUTHOR',0),(14,4,'AUTHOR',0),
(15,5,'AUTHOR',0),
(16,12,'AUTHOR',0),
(17,11,'AUTHOR',0),(18,11,'AUTHOR',0),
-- Kinh tế
(25,7,'AUTHOR',0),(27,7,'AUTHOR',0),
(26,25,'AUTHOR',0),
-- Kỹ năng sống
(37,6,'AUTHOR',0),(38,6,'AUTHOR',0),
(39,14,'AUTHOR',0),
-- Khoa học
(47,9,'AUTHOR',0),(48,9,'AUTHOR',0),
-- Lịch sử
-- Manga
(85,16,'AUTHOR',0),(86,16,'AUTHOR',0),
(87,17,'AUTHOR',0),(88,17,'AUTHOR',0),
(89,18,'AUTHOR',0)
GO

-- ===================== BOOK VARIANTS (mỗi sách 1 variant) =====================

INSERT INTO book_variants (book_id, sku, format, list_price, sale_price, page_count, weight_grams, is_active) VALUES
-- Văn học VN (1-12)
(1,'VH-VN-001','PAPERBACK',89000,78000,378,350,1),
(2,'VH-VN-002','PAPERBACK',95000,85000,300,320,1),
(3,'VH-VN-003','PAPERBACK',75000,68000,216,280,1),
(4,'VH-VN-004','PAPERBACK',45000,39000,192,250,1),
(5,'VH-VN-005','PAPERBACK',35000,30000,96,150,1),
(6,'VH-VN-006','PAPERBACK',72000,65000,208,290,1),
-- Văn học nước ngoài (13-24)
(13,'VH-NN-001','PAPERBACK',120000,99000,420,450,1),
(14,'VH-NN-002','PAPERBACK',135000,115000,480,480,1),
(15,'VH-NN-003','PAPERBACK',69000,59000,228,280,1),
(16,'VH-NN-004','HARDCOVER',85000,75000,112,250,1),
(17,'VH-NN-005','PAPERBACK',98000,85000,328,360,1),
(18,'VH-NN-006','PAPERBACK',75000,65000,152,200,1),
-- Kinh tế (25-36)
(25,'KT-001','PAPERBACK',110000,89000,336,380,1),
(26,'KT-002','PAPERBACK',95000,79000,304,350,1),
(27,'KT-003','PAPERBACK',99000,85000,280,330,1),
(28,'KT-004','HARDCOVER',155000,129000,400,480,1),
(29,'KT-005','PAPERBACK',125000,105000,320,370,1),
(30,'KT-006','PAPERBACK',135000,115000,416,430,1),
-- Kỹ năng sống (37-46)
(37,'KN-001','PAPERBACK',86000,72000,320,350,1),
(38,'KN-002','PAPERBACK',79000,65000,304,330,1),
(39,'KN-003','PAPERBACK',99000,85000,248,290,1),
(40,'KN-004','PAPERBACK',89000,75000,280,310,1),
(41,'KN-005','PAPERBACK',125000,105000,420,460,1),
-- Khoa học (47-56)
(47,'KH-001','PAPERBACK',145000,125000,256,320,1),
(48,'KH-002','HARDCOVER',175000,149000,224,380,1),
(49,'KH-003','PAPERBACK',350000,299000,464,520,1),
(50,'KH-004','PAPERBACK',380000,329000,352,480,1),
(51,'KH-005','PAPERBACK',125000,105000,400,440,1),
-- Thiếu nhi (57-66)
(57,'TN-001','HARDCOVER',95000,82000,192,320,1),
(58,'TN-002','PAPERBACK',75000,65000,288,310,1),
(59,'TN-003','PAPERBACK',65000,55000,176,230,1),
(60,'TN-004','PAPERBACK',59000,49000,160,210,1),
(61,'TN-005','PAPERBACK',49000,42000,200,250,1),
-- Tâm lý (67-76)
(67,'TL-001','PAPERBACK',69000,58000,192,250,1),
(68,'TL-002','PAPERBACK',85000,72000,256,300,1),
(69,'TL-003','PAPERBACK',129000,109000,680,720,1),
(70,'TL-004','PAPERBACK',119000,99000,320,370,1),
(71,'TL-005','PAPERBACK',95000,82000,280,320,1),
-- Lịch sử (77-84)
(77,'LS-001','HARDCOVER',125000,105000,160,350,1),
(78,'LS-002','HARDCOVER',350000,299000,1200,1500,1),
(79,'LS-003','PAPERBACK',175000,149000,496,560,1),
(80,'LS-004','PAPERBACK',95000,82000,320,370,1),
-- Manga (85-94)
(85,'MG-001','PAPERBACK',25000,22000,200,160,1),
(86,'MG-002','PAPERBACK',25000,22000,200,160,1),
(87,'MG-003','PAPERBACK',25000,22000,192,155,1),
(88,'MG-004','PAPERBACK',25000,22000,192,155,1),
(89,'MG-005','PAPERBACK',22000,19000,180,140,1),
-- Sách giáo khoa (95-100)
(95,'SGK-001','PAPERBACK',85000,72000,320,380,1),
(96,'SGK-002','PAPERBACK',145000,125000,400,450,1),
(97,'SGK-003','PAPERBACK',350000,299000,256,350,1)
GO

-- ===================== BOOK IMAGES =====================
-- Dán link ảnh thật vào cột url cho từng cuốn.
-- Có thể thêm nhiều ảnh cho 1 book bằng cách tăng sort_order; ảnh cover để is_cover = 1.

INSERT INTO book_images (book_id, url, alt_text, is_cover, sort_order) VALUES
(1, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774506138/OIP_neyaha.webp', N'Tôi thấy hoa vàng trên cỏ xanh', 1, 0),
(2, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774506352/1574221778_5450_main_jmf7jj.jpg', N'Mắt biếc', 1, 0),
(3, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774506405/10925109_g42pvj.jpg', N'Cho tôi xin một vé đi tuổi thơ', 1, 0),
(4, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774575880/download_wskrea.jpg', N'Dế mèn phiêu lưu ký', 1, 0),
(5, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774575908/images_s0euay.webp', N'Chí Phèo', 1, 0),
(6, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774575993/images_vudapd.webp', N'Cánh đồng bất tận', 1, 0),
(13, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774576812/download_ytsynl.jpg', N'Rừng Na Uy', 1, 0),
(14, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774576834/shopping_ysmfwa.webp', N'Kafka bên bờ biển', 1, 0),
(15, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774576861/download_lc4jjc.jpg', N'Nhà giả kim', 1, 0),
(16, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774576885/shopping_bnhhis.webp', N'Hoàng tử bé', 1, 0),
(17, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774576909/download_iyiefw.jpg', N'1984', 1, 0),
(18, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774576955/download_dgup55.jpg', N'Trại súc vật', 1, 0),
(25, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774576990/shopping_ogpwvv.webp', N'Cha giàu cha nghèo', 1, 0),
(26, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577009/shopping_d2envd.webp', N'Nghĩ giàu làm giàu', 1, 0),
(27, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577032/shopping_wlhkpt.webp', N'Dạy con làm giàu - Tập 1', 1, 0),
(28, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577072/shopping_hnemjy.webp', N'Từ tốt đến vĩ đại', 1, 0),
(29, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577092/shopping_hpcege.webp', N'Khởi nghiệp tinh gọn', 1, 0),
(30, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577117/shopping_mm7u7q.webp', N'Tuần làm việc 4 giờ', 1, 0),
(37, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577241/shopping_ub8tw4.webp', N'Đắc nhân tâm', 1, 0),
(38, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577259/shopping_ytkav0.webp', N'Quẳng gánh lo đi và vui sống', 1, 0),
(39, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577303/download_drn9bz.png', N'Nghệ thuật tinh tế của việc đếch quan tâm', 1, 0),
(40, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577324/download_gs4djl.jpg', N'Bí mật tư duy triệu phú', 1, 0),
(41, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577344/download_sn3cox.png', N'7 thói quen hiệu quả', 1, 0),
(47, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577363/download_s9k2ko.jpg', N'Lược sử thời gian', 1, 0),
(48, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577386/download_qkwmvp.jpg', N'Vũ trụ trong vỏ hạt dẻ', 1, 0),
(49, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577405/download_uoocru.jpg', N'Clean Code', 1, 0),
(50, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577439/download_dnaynm.jpg', N'The Pragmatic Programmer', 1, 0),
(51, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577471/download_xdgh7t.png', N'Giải thuật và lập trình', 1, 0),
(57, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577493/shopping_sgah2q.webp', N'Dế mèn phiêu lưu ký (bìa cứng)', 1, 0),
(58, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577513/shopping_ou1mrr.webp', N'Totto-chan bên cửa sổ', 1, 0),
(59, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577535/shopping_jcvqtb.webp', N'Charlie và nhà máy Sôcôla', 1, 0),
(60, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577553/shopping_e2lt9s.webp', N'Pippi tất dài', 1, 0),
(61, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577660/shopping_zpj4so.webp', N'Kính vạn hoa - Tập 1', 1, 0),
(67, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577689/shopping_dt9ayg.webp', N'Tâm lý học đám đông', 1, 0),
(68, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774577793/download_uozckx.jpg', N'Ý nghĩa cuộc sống', 1, 0),
(69, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774579055/shopping_go2p0k.webp', N'Tội ác và hình phạt', 1, 0),
(70, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774579100/shopping_eoxqu2.webp', N'Flow - Dòng chảy', 1, 0),
(71, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774579122/download_anweei.jpg', N'Triết học hiện sinh', 1, 0),
(77, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774579145/shopping_bfarpp.webp', N'Lịch sử Việt Nam bằng tranh', 1, 0),
(78, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774579164/shopping_fhummb.webp', N'Đại Việt sử ký toàn thư', 1, 0),
(79, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774579184/download_vlovb7.jpg', N'Guns, Germs and Steel', 1, 0),
(80, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774579210/download_obnt5m.jpg', N'Nghìn năm văn hiến', 1, 0),
(85, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774579232/download_egt425.jpg', N'One Piece - Tập 1', 1, 0),
(86, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774579250/download_ewiprr.jpg', N'One Piece - Tập 2', 1, 0),
(87, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774579270/download_timh6z.jpg', N'Conan - Tập 1', 1, 0),
(88, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774579293/download_md0zgt.jpg', N'Conan - Tập 2', 1, 0),
(89, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774579321/download_lulnef.jpg', N'Doraemon - Tập 1', 1, 0),
(95, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774579349/download_jp5y4h.jpg', N'Toán cao cấp - Tập 1', 1, 0),
(96, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774579373/download_avqxcu.jpg', N'Ngữ pháp tiếng Anh nâng cao', 1, 0),
(97, N'https://res.cloudinary.com/dg4uqpioc/image/upload/v1774579396/shopping_l7bkn3.webp', N'IELTS Academic 18', 1, 0);

GO

PRINT N'✅ Seed complete: 10 categories, 25 authors, 50 books, 50 variants, placeholders for image links';
