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
(7,  1, N'Số đỏ',                                   'so-do',                                   N'NXB Văn Học', 2018, 'vi', N'Tiểu thuyết trào phúng của Vũ Trọng Phụng', 'ACTIVE'),
(8,  1, N'Tướng về hưu',                             'tuong-ve-huu',                             N'NXB Văn Học', 2019, 'vi', N'Truyện ngắn đương đại Việt Nam', 'ACTIVE'),
(9,  1, N'Những ngôi sao xa xôi',                   'nhung-ngoi-sao-xa-xoi',                   N'NXB Văn Học', 2017, 'vi', N'Truyện ngắn thời chiến', 'ACTIVE'),
(10, 1, N'Ngồi khóc trên cây',                      'ngoi-khoc-tren-cay',                      N'NXB Trẻ', 2021, 'vi', N'Tiểu thuyết Nguyễn Nhật Ánh', 'ACTIVE'),
(11, 1, N'Đi qua hoa cúc',                          'di-qua-hoa-cuc',                          N'NXB Trẻ', 2020, 'vi', N'Thơ Nguyễn Phong Việt', 'ACTIVE'),
(12, 1, N'Lão Hạc',                                 'lao-hac',                                 N'NXB Văn Học', 2016, 'vi', N'Truyện ngắn kinh điển Nam Cao', 'ACTIVE'),

-- Văn học nước ngoài (13-24)
(13, 2, N'Rừng Na Uy',                              'rung-na-uy',                              N'NXB Hội Nhà Văn', 2020, 'vi', N'Tiểu thuyết nổi tiếng của Murakami', 'ACTIVE'),
(14, 2, N'Kafka bên bờ biển',                       'kafka-ben-bo-bien',                       N'NXB Hội Nhà Văn', 2019, 'vi', N'Hành trình tìm kiếm bản thể', 'ACTIVE'),
(15, 2, N'Nhà giả kim',                             'nha-gia-kim',                             N'NXB Hội Nhà Văn', 2020, 'vi', N'Câu chuyện về theo đuổi giấc mơ', 'ACTIVE'),
(16, 2, N'Hoàng tử bé',                             'hoang-tu-be',                             N'NXB Kim Đồng', 2019, 'vi', N'Tác phẩm kinh điển về tình bạn', 'ACTIVE'),
(17, 2, N'1984',                                    'mot-chin-tam-tu',                          N'NXB Hội Nhà Văn', 2021, 'vi', N'Tiểu thuyết dystopia kinh điển', 'ACTIVE'),
(18, 2, N'Trại súc vật',                            'trai-suc-vat',                            N'NXB Hội Nhà Văn', 2020, 'vi', N'Ngụ ngôn chính trị sắc sảo', 'ACTIVE'),
(19, 2, N'Bắt trẻ đồng xanh',                      'bat-tre-dong-xanh',                       N'NXB Văn Học', 2019, 'vi', N'Tiểu thuyết tuổi trẻ nổi loạn', 'ACTIVE'),
(20, 2, N'Người đua diều',                          'nguoi-dua-dieu',                          N'NXB Hội Nhà Văn', 2018, 'vi', N'Tình bạn và sự cứu chuộc', 'ACTIVE'),
(21, 2, N'Đi tìm lẽ sống',                          'di-tim-le-song',                          N'NXB Trẻ', 2020, 'vi', N'Hồi ký từ trại tập trung', 'ACTIVE'),
(22, 2, N'Cuộc sống không giới hạn',                'cuoc-song-khong-gioi-han',                N'NXB Trẻ', 2021, 'vi', N'Câu chuyện truyền cảm hứng', 'ACTIVE'),
(23, 2, N'Sapiens - Lược sử loài người',            'sapiens-luoc-su-loai-nguoi',              N'NXB Thế Giới', 2022, 'vi', N'Lịch sử loài người từ thời cổ đại', 'ACTIVE'),
(24, 2, N'Homo Deus - Lược sử tương lai',           'homo-deus-luoc-su-tuong-lai',             N'NXB Thế Giới', 2022, 'vi', N'Tương lai của loài người', 'ACTIVE'),

-- Kinh tế - Kinh doanh (25-36)
(25, 3, N'Cha giàu cha nghèo',                     'cha-giau-cha-ngheo',                     N'NXB Trẻ', 2019, 'vi', N'Bài học tài chính từ hai người cha', 'ACTIVE'),
(26, 3, N'Nghĩ giàu làm giàu',                     'nghi-giau-lam-giau',                     N'NXB Trẻ', 2018, 'vi', N'13 nguyên tắc làm giàu bất biến', 'ACTIVE'),
(27, 3, N'Dạy con làm giàu - Tập 1',               'day-con-lam-giau-tap-1',                 N'NXB Trẻ', 2020, 'vi', N'Phần tiếp theo của Cha giàu cha nghèo', 'ACTIVE'),
(28, 3, N'Từ tốt đến vĩ đại',                      'tu-tot-den-vi-dai',                      N'NXB Trẻ', 2019, 'vi', N'Good to Great - Jim Collins', 'ACTIVE'),
(29, 3, N'Khởi nghiệp tinh gọn',                   'khoi-nghiep-tinh-gon',                   N'NXB Trẻ', 2020, 'vi', N'Lean Startup methodology', 'ACTIVE'),
(30, 3, N'Tuần làm việc 4 giờ',                     'tuan-lam-viec-4-gio',                     N'NXB Lao Động', 2019, 'vi', N'Tối ưu hóa thời gian và hiệu suất', 'ACTIVE'),
(31, 3, N'Zero to One',                             'zero-to-one',                             N'NXB Trẻ', 2021, 'vi', N'Bí quyết khởi nghiệp của Peter Thiel', 'ACTIVE'),
(32, 3, N'Quản trị Marketing',                      'quan-tri-marketing',                      N'NXB Đại Học Kinh Tế', 2020, 'vi', N'Giáo trình marketing Philip Kotler', 'ACTIVE'),
(33, 3, N'Chiến lược đại dương xanh',               'chien-luoc-dai-duong-xanh',               N'NXB Trẻ', 2019, 'vi', N'Blue Ocean Strategy', 'ACTIVE'),
(34, 3, N'Binh pháp Tôn Tử trong kinh doanh',      'binh-phap-ton-tu-kinh-doanh',            N'NXB Thế Giới', 2020, 'vi', N'Áp dụng binh pháp vào business', 'ACTIVE'),
(35, 3, N'Tư duy nhanh và chậm',                    'tu-duy-nhanh-va-cham',                    N'NXB Thế Giới', 2021, 'vi', N'Thinking Fast and Slow', 'ACTIVE'),
(36, 3, N'Quốc gia khởi nghiệp',                   'quoc-gia-khoi-nghiep',                   N'NXB Thế Giới', 2020, 'vi', N'Câu chuyện về nền kinh tế Israel', 'ACTIVE'),

-- Kỹ năng sống (37-46)
(37, 4, N'Đắc nhân tâm',                           'dac-nhan-tam',                           N'NXB Tổng Hợp', 2020, 'vi', N'Nghệ thuật thu phục lòng người', 'ACTIVE'),
(38, 4, N'Quẳng gánh lo đi và vui sống',            'quang-ganh-lo-di-va-vui-song',            N'NXB Tổng Hợp', 2019, 'vi', N'Bí quyết sống vui vẻ', 'ACTIVE'),
(39, 4, N'Nghệ thuật tinh tế của việc đếch quan tâm', 'nghe-thuat-dech-quan-tam',             N'NXB Trẻ', 2021, 'vi', N'The Subtle Art of Not Giving a F*ck', 'ACTIVE'),
(40, 4, N'Bí mật tư duy triệu phú',                'bi-mat-tu-duy-trieu-phu',                N'NXB Trẻ', 2019, 'vi', N'Secrets of the Millionaire Mind', 'ACTIVE'),
(41, 4, N'7 thói quen hiệu quả',                   '7-thoi-quen-hieu-qua',                   N'NXB Trẻ', 2020, 'vi', N'7 Habits of Highly Effective People', 'ACTIVE'),
(42, 4, N'Sức mạnh của thói quen',                 'suc-manh-cua-thoi-quen',                 N'NXB Lao Động', 2021, 'vi', N'The Power of Habit', 'ACTIVE'),
(43, 4, N'Atomic Habits - Thay đổi tí hon',         'atomic-habits-thay-doi-ti-hon',           N'NXB Thế Giới', 2022, 'vi', N'Xây dựng thói quen tốt', 'ACTIVE'),
(44, 4, N'Đọc vị bất kỳ ai',                       'doc-vi-bat-ky-ai',                       N'NXB Lao Động', 2020, 'vi', N'Kỹ năng đọc ngôn ngữ cơ thể', 'ACTIVE'),
(45, 4, N'Think Again - Dám nghĩ lại',              'think-again-dam-nghi-lai',                N'NXB Trẻ', 2022, 'vi', N'Sức mạnh của việc thay đổi tư duy', 'ACTIVE'),
(46, 4, N'Không phàn nàn',                          'khong-phan-nan',                          N'NXB Tổng Hợp', 2019, 'vi', N'21 ngày thay đổi cuộc sống', 'ACTIVE'),

-- Khoa học - Công nghệ (47-56)
(47, 5, N'Lược sử thời gian',                      'luoc-su-thoi-gian',                      N'NXB Trẻ', 2020, 'vi', N'A Brief History of Time', 'ACTIVE'),
(48, 5, N'Vũ trụ trong vỏ hạt dẻ',                 'vu-tru-trong-vo-hat-de',                 N'NXB Trẻ', 2019, 'vi', N'The Universe in a Nutshell', 'ACTIVE'),
(49, 5, N'Clean Code',                              'clean-code',                              N'NXB Bách Khoa', 2021, 'en', N'Hướng dẫn viết code sạch', 'ACTIVE'),
(50, 5, N'The Pragmatic Programmer',                'the-pragmatic-programmer',                N'NXB Bách Khoa', 2022, 'en', N'Lập trình viên thực dụng', 'ACTIVE'),
(51, 5, N'Giải thuật và lập trình',                 'giai-thuat-va-lap-trinh',                N'NXB Đại Học Quốc Gia', 2020, 'vi', N'Cấu trúc dữ liệu và giải thuật', 'ACTIVE'),
(52, 5, N'Trí tuệ nhân tạo',                       'tri-tue-nhan-tao',                       N'NXB Bách Khoa', 2022, 'vi', N'AI và Machine Learning cơ bản', 'ACTIVE'),
(53, 5, N'Blockchain và tiền mã hóa',              'blockchain-va-tien-ma-hoa',              N'NXB Thế Giới', 2021, 'vi', N'Tìm hiểu về blockchain', 'ACTIVE'),
(54, 5, N'Python cho người mới bắt đầu',            'python-cho-nguoi-moi',                    N'NXB Bách Khoa', 2022, 'vi', N'Học Python từ zero', 'ACTIVE'),
(55, 5, N'Cosmos - Vũ trụ',                         'cosmos-vu-tru',                           N'NXB Trẻ', 2020, 'vi', N'Carl Sagan - Khám phá vũ trụ', 'ACTIVE'),
(56, 5, N'Hóa học vui',                             'hoa-hoc-vui',                             N'NXB Kim Đồng', 2019, 'vi', N'Khoa học thú vị cho mọi lứa tuổi', 'ACTIVE');

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
(1,1,'AUTHOR',0),(2,1,'AUTHOR',0),(3,1,'AUTHOR',0),(10,1,'AUTHOR',0),(61,1,'AUTHOR',0),
(4,2,'AUTHOR',0),(57,2,'AUTHOR',0),
(5,3,'AUTHOR',0),(12,3,'AUTHOR',0),
(6,13,'AUTHOR',0),
(8,22,'AUTHOR',0),(9,23,'AUTHOR',0),(11,19,'AUTHOR',0),
-- Văn học nước ngoài
(13,4,'AUTHOR',0),(14,4,'AUTHOR',0),
(15,5,'AUTHOR',0),
(16,12,'AUTHOR',0),
(17,11,'AUTHOR',0),(18,11,'AUTHOR',0),
(23,8,'AUTHOR',0),(24,8,'AUTHOR',0),
-- Kinh tế
(25,7,'AUTHOR',0),(27,7,'AUTHOR',0),
(26,25,'AUTHOR',0),
(34,24,'AUTHOR',0),
(35,15,'AUTHOR',0),
-- Kỹ năng sống
(37,6,'AUTHOR',0),(38,6,'AUTHOR',0),
(39,14,'AUTHOR',0),
(45,20,'AUTHOR',0),
-- Khoa học
(47,9,'AUTHOR',0),(48,9,'AUTHOR',0),
-- Lịch sử
(81,21,'AUTHOR',0),(82,21,'AUTHOR',0),
-- Manga
(85,16,'AUTHOR',0),(86,16,'AUTHOR',0),
(87,17,'AUTHOR',0),(88,17,'AUTHOR',0),
(89,18,'AUTHOR',0),(90,18,'AUTHOR',0);

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
(7,'VH-VN-007','PAPERBACK',65000,58000,320,340,1),
(8,'VH-VN-008','PAPERBACK',55000,48000,180,240,1),
(9,'VH-VN-009','PAPERBACK',42000,38000,120,180,1),
(10,'VH-VN-010','PAPERBACK',85000,75000,280,310,1),
(11,'VH-VN-011','PAPERBACK',79000,69000,160,220,1),
(12,'VH-VN-012','PAPERBACK',32000,28000,80,130,1),
-- Văn học nước ngoài (13-24)
(13,'VH-NN-001','PAPERBACK',120000,99000,420,450,1),
(14,'VH-NN-002','PAPERBACK',135000,115000,480,480,1),
(15,'VH-NN-003','PAPERBACK',69000,59000,228,280,1),
(16,'VH-NN-004','HARDCOVER',85000,75000,112,250,1),
(17,'VH-NN-005','PAPERBACK',98000,85000,328,360,1),
(18,'VH-NN-006','PAPERBACK',75000,65000,152,200,1),
(19,'VH-NN-007','PAPERBACK',89000,79000,280,320,1),
(20,'VH-NN-008','PAPERBACK',105000,89000,370,400,1),
(21,'VH-NN-009','PAPERBACK',85000,72000,196,260,1),
(22,'VH-NN-010','PAPERBACK',95000,82000,240,300,1),
(23,'VH-NN-011','PAPERBACK',189000,159000,560,620,1),
(24,'VH-NN-012','PAPERBACK',179000,149000,480,550,1),
-- Kinh tế (25-36)
(25,'KT-001','PAPERBACK',110000,89000,336,380,1),
(26,'KT-002','PAPERBACK',95000,79000,304,350,1),
(27,'KT-003','PAPERBACK',99000,85000,280,330,1),
(28,'KT-004','HARDCOVER',155000,129000,400,480,1),
(29,'KT-005','PAPERBACK',125000,105000,320,370,1),
(30,'KT-006','PAPERBACK',135000,115000,416,430,1),
(31,'KT-007','PAPERBACK',109000,92000,240,300,1),
(32,'KT-008','PAPERBACK',185000,159000,720,850,1),
(33,'KT-009','PAPERBACK',145000,125000,368,420,1),
(34,'KT-010','PAPERBACK',79000,65000,196,250,1),
(35,'KT-011','PAPERBACK',169000,145000,520,580,1),
(36,'KT-012','PAPERBACK',129000,109000,360,400,1),
-- Kỹ năng sống (37-46)
(37,'KN-001','PAPERBACK',86000,72000,320,350,1),
(38,'KN-002','PAPERBACK',79000,65000,304,330,1),
(39,'KN-003','PAPERBACK',99000,85000,248,290,1),
(40,'KN-004','PAPERBACK',89000,75000,280,310,1),
(41,'KN-005','PAPERBACK',125000,105000,420,460,1),
(42,'KN-006','PAPERBACK',109000,92000,380,400,1),
(43,'KN-007','PAPERBACK',139000,119000,320,360,1),
(44,'KN-008','PAPERBACK',75000,62000,240,270,1),
(45,'KN-009','PAPERBACK',129000,109000,308,350,1),
(46,'KN-010','PAPERBACK',69000,58000,200,240,1),
-- Khoa học (47-56)
(47,'KH-001','PAPERBACK',145000,125000,256,320,1),
(48,'KH-002','HARDCOVER',175000,149000,224,380,1),
(49,'KH-003','PAPERBACK',350000,299000,464,520,1),
(50,'KH-004','PAPERBACK',380000,329000,352,480,1),
(51,'KH-005','PAPERBACK',125000,105000,400,440,1),
(52,'KH-006','PAPERBACK',165000,139000,320,380,1),
(53,'KH-007','PAPERBACK',135000,115000,280,330,1),
(54,'KH-008','PAPERBACK',155000,129000,360,400,1),
(55,'KH-009','PAPERBACK',145000,119000,340,380,1),
(56,'KH-010','PAPERBACK',65000,55000,180,220,1),
-- Thiếu nhi (57-66)
(57,'TN-001','HARDCOVER',95000,82000,192,320,1),
(58,'TN-002','PAPERBACK',75000,65000,288,310,1),
(59,'TN-003','PAPERBACK',65000,55000,176,230,1),
(60,'TN-004','PAPERBACK',59000,49000,160,210,1),
(61,'TN-005','PAPERBACK',49000,42000,200,250,1),
(62,'TN-006','HARDCOVER',89000,75000,240,350,1),
(63,'TN-007','PAPERBACK',69000,59000,320,350,1),
(64,'TN-008','PAPERBACK',55000,45000,144,190,1),
(65,'TN-009','PAPERBACK',45000,38000,128,170,1),
(66,'TN-010','PAPERBACK',25000,20000,96,120,1),
-- Tâm lý (67-76)
(67,'TL-001','PAPERBACK',69000,58000,192,250,1),
(68,'TL-002','PAPERBACK',85000,72000,256,300,1),
(69,'TL-003','PAPERBACK',129000,109000,680,720,1),
(70,'TL-004','PAPERBACK',119000,99000,320,370,1),
(71,'TL-005','PAPERBACK',95000,82000,280,320,1),
(72,'TL-006','PAPERBACK',105000,89000,300,340,1),
(73,'TL-007','PAPERBACK',75000,62000,184,230,1),
(74,'TL-008','PAPERBACK',89000,75000,240,280,1),
(75,'TL-009','PAPERBACK',115000,95000,360,400,1),
(76,'TL-010','PAPERBACK',69000,58000,176,220,1),
-- Lịch sử (77-84)
(77,'LS-001','HARDCOVER',125000,105000,160,350,1),
(78,'LS-002','HARDCOVER',350000,299000,1200,1500,1),
(79,'LS-003','PAPERBACK',175000,149000,496,560,1),
(80,'LS-004','PAPERBACK',95000,82000,320,370,1),
(81,'LS-005','PAPERBACK',155000,129000,656,700,1),
(82,'LS-006','PAPERBACK',185000,155000,688,740,1),
(83,'LS-007','PAPERBACK',195000,165000,1200,1300,1),
(84,'LS-008','PAPERBACK',165000,139000,960,1000,1),
-- Manga (85-94)
(85,'MG-001','PAPERBACK',25000,22000,200,160,1),
(86,'MG-002','PAPERBACK',25000,22000,200,160,1),
(87,'MG-003','PAPERBACK',25000,22000,192,155,1),
(88,'MG-004','PAPERBACK',25000,22000,192,155,1),
(89,'MG-005','PAPERBACK',22000,19000,180,140,1),
(90,'MG-006','PAPERBACK',22000,19000,180,140,1),
(91,'MG-007','PAPERBACK',25000,22000,208,165,1),
(92,'MG-008','PAPERBACK',25000,22000,200,160,1),
(93,'MG-009','PAPERBACK',30000,26000,196,160,1),
(94,'MG-010','PAPERBACK',25000,22000,184,150,1),
-- Sách giáo khoa (95-100)
(95,'SGK-001','PAPERBACK',85000,72000,320,380,1),
(96,'SGK-002','PAPERBACK',145000,125000,400,450,1),
(97,'SGK-003','PAPERBACK',350000,299000,256,350,1),
(98,'SGK-004','PAPERBACK',95000,82000,360,400,1),
(99,'SGK-005','PAPERBACK',75000,65000,280,320,1),
(100,'SGK-006','PAPERBACK',165000,139000,320,380,1);

GO

-- ===================== BOOK IMAGES (placeholder Cloudinary URLs) =====================

INSERT INTO book_images (book_id, url, alt_text, is_cover, sort_order)
SELECT b.id,
       'https://res.cloudinary.com/du8cq6vfb/image/upload/v1/bookshop/placeholder_' + CAST(b.id AS VARCHAR) + '.jpg',
       b.title,
       1,
       0
FROM books b WHERE b.id BETWEEN 1 AND 100;

GO

PRINT N'✅ Seed complete: 10 categories, 25 authors, 100 books, 100 variants, 100 images';
