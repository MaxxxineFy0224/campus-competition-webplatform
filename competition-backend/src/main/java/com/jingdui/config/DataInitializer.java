package com.jingdui.config;

import com.jingdui.entity.Competition;
import com.jingdui.entity.TeamPost;
import com.jingdui.entity.User;
import com.jingdui.mapper.CompetitionMapper;
import com.jingdui.mapper.TeamPostMapper;
import com.jingdui.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据初始化器 —— 启动时自动插入种子数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final CompetitionMapper competitionMapper;
    private final TeamPostMapper teamPostMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userMapper.selectCount(null) > 0) {
            log.info("数据已存在，跳过初始化");
            return;
        }

        String defaultPasswordHash = passwordEncoder.encode("123456");
        LocalDateTime now = LocalDateTime.now();

        // ===== 插入用户 =====
        List<User> users = List.of(
                createUser("张三", defaultPasswordHash, "清华大学", "计算机科学与技术", "大三",
                        "热爱编程，参加过多次算法竞赛，喜欢钻研技术。", "Java,Python,算法", now),
                createUser("李四", defaultPasswordHash, "北京大学", "软件工程", "大二",
                        "擅长前端开发和UI设计，对用户体验有独到见解。", "前端开发,UI设计,Vue.js,React", now),
                createUser("王五", defaultPasswordHash, "复旦大学", "数学与应用数学", "大四",
                        "数学系学霸，多次获得数学建模奖项，擅长数据分析和论文写作。", "数据分析,数学建模,MATLAB,Python", now)
        );
        for (User user : users) {
            userMapper.insert(user);
        }
        log.info("已插入 {} 个用户（默认密码: 123456）", users.size());

        // ===== 插入竞赛 =====
        List<Competition> competitions = List.of(
                createComp(1L, "全国大学生数学建模竞赛", "数学建模", "国家级", "中国工业与应用数学学会",
                        "2026-09-15", "2026-09-20", "线上+线下", 3, 3,
                        "全国大学生数学建模竞赛创办于1992年，每年一届，是首批列入\"高校学科竞赛排行榜\"的19项竞赛之一。竞赛面向全国大专院校的学生，旨在培养学生的创新意识、团队精神和运用数学方法解决实际问题的能力。",
                        "http://www.mcm.edu.cn", "国奖、省奖", now),
                createComp(2L, "ACM-ICPC 国际大学生程序设计竞赛", "编程算法", "国际级", "ACM 协会",
                        "2026-10-10", "2026-11-15", "线上初赛+线下区域赛", 3, 3,
                        "ACM-ICPC 是世界上公认的规模最大、水平最高的国际大学生程序设计竞赛，旨在展示大学生在压力下编写程序、分析和解决问题的能力。每年有来自全球数千所大学的队伍参赛，被誉为\"编程界的奥林匹克\"。",
                        "https://icpc.global", "金牌、银牌、铜牌", now),
                createComp(3L, "全国大学生电子设计竞赛", "电子设计", "国家级", "教育部高等教育司",
                        "2026-08-30", "2026-09-10", "各省赛区", 3, 3,
                        "全国大学生电子设计竞赛是教育部倡导的四大学科竞赛之一，旨在培养大学生的实践创新意识与团队协作精神。竞赛内容涵盖模拟电路、数字电路、嵌入式系统等多个方向，是电子信息类专业含金量最高的赛事之一。",
                        "https://www.nuedc.org", "国奖、省奖", now),
                createComp(4L, "RoboMaster 机甲大师赛", "机器人", "国家级", "大疆创新科技有限公司",
                        "2026-12-01", "2027-03-15", "深圳", 5, 8,
                        "RoboMaster 机甲大师赛是由大疆创新发起并承办的全球性机器人赛事，融合了机器视觉、嵌入式系统设计、机械控制等多学科技术。比赛采用对抗形式，参赛队伍需自主研发全自动和半自动机器人进行对战。",
                        "https://www.robomaster.com", "冠亚季军、分项奖", now),
                createComp(5L, "\"外研社杯\"全国英语演讲大赛", "语言文学", "国家级", "外语教学与研究出版社",
                        "2026-11-20", "2026-12-10", "北京", 1, 1,
                        "\"外研社杯\"全国英语演讲大赛是国内规模最大、权威性最高的英语演讲赛事之一。大赛以\"讲好中国故事\"为主题，考察选手的英语表达能力、逻辑思维能力和跨文化交际能力，是英语专业和非英语专业学生展示自我的绝佳平台。",
                        "https://www.fltrp.com", "特等奖、一等奖、二等奖、三等奖", now),
                createComp(6L, "全国大学生信息安全竞赛", "信息安全", "国家级", "教育部高等学校网络空间安全专业教学指导委员会",
                        "2026-09-05", "2026-10-15", "线上CTF", 3, 4,
                        "全国大学生信息安全竞赛旨在培养、选拔、推荐优秀信息安全专业人才，促进高等学校信息安全专业课程体系改革。竞赛以CTF夺旗赛为主要形式，涵盖Web安全、二进制安全、密码学、逆向工程等多个方向。",
                        "https://www.ciscn.cn", "一等奖、二等奖、三等奖", now),
                createComp(7L, "全国大学生智能硬件设计大赛", "智能硬件", "国家级", "工业和信息化部人才交流中心",
                        "2026-10-20", "2026-11-20", "北京", 3, 5,
                        "全国大学生智能硬件设计大赛面向全国高校本科及研究生，聚焦智能硬件产品的设计与开发。参赛者需完成从需求分析到软硬件协同设计再到成品展示的全流程，优秀作品将获得孵化支持与产业对接机会。",
                        "https://www.smart-hardware.cn", "国奖、省奖", now),
                createComp(8L, "中国大学生计算机设计大赛", "计算机设计", "国家级", "教育部高等学校计算机类专业教学指导委员会",
                        "2026-08-15", "2026-09-20", "线上评审", 1, 3,
                        "中国大学生计算机设计大赛是面向全国高校本科生的计算机类权威赛事，涵盖软件应用开发、微课与教学辅助、物联网应用、大数据应用、人工智能应用、数字媒体设计等多个类别，旨在培养大学生的创新能力和实践能力。",
                        "https://www.cccc.edu.cn", "一等奖、二等奖、三等奖", now),
                createComp(9L, "\"互联网+\"大学生创新创业大赛", "创新创业", "国家级", "教育部",
                        "2026-12-15", "2027-03-01", "线上评审+线下路演", 1, 10,
                        "\"互联网+\"大学生创新创业大赛是国内规模最大、影响力最广的大学生创新创业赛事，覆盖全国所有高校。大赛以\"互联网+\"为主题，鼓励学生将互联网技术与传统行业深度融合，推动高校创新创业教育改革。",
                        "https://cy.ncss.cn", "金奖、银奖、铜奖", now),
                createComp(10L, "全国大学生广告艺术大赛", "设计传媒", "国家级", "中国高等教育学会",
                        "2026-11-01", "2026-12-05", "线上评审", 1, 5,
                        "全国大学生广告艺术大赛（大广赛）是规模大、覆盖面广、参与师生人数多、作品水准高的全国性高校文科竞赛。大赛涵盖平面广告、视频广告、广播广告、互动广告等多个类别，是广告与传媒方向最具代表性的赛事。",
                        "https://www.daguangsai.com", "一等奖、二等奖、三等奖及优秀奖", now),
                createComp(11L, "全国大学生数学竞赛", "数学建模", "国家级", "中国数学会",
                        "2026-10-25", "2026-11-20", "各高校", 1, 1,
                        "全国大学生数学竞赛是中国数学会主办的面向本科生的全国性高水平学科竞赛。竞赛分为数学专业组和非数学专业组，考察内容包括高等数学、线性代数、概率论等核心数学知识，旨在培养大学生的数学素养和逻辑思维能力。",
                        "https://www.cms.org.cn", "一等奖、二等奖、三等奖", now),
                createComp(12L, "全国大学生机器人大赛", "机器人", "国家级", "共青团中央",
                        "2026-09-25", "2026-11-10", "北京", 3, 6,
                        "全国大学生机器人大赛（ROBOCON）是国内最具影响力的机器人赛事之一，采用年度主题赛制，每年发布不同的竞赛任务。参赛队伍需自主设计制造机器人完成指定任务，全面考察机械、电子、控制、算法等多方面的综合能力。",
                        "https://www.cnrobocon.org", "冠亚季军、最佳技术奖", now)
        );
        for (Competition c : competitions) {
            competitionMapper.insert(c);
        }
        log.info("已插入 {} 条竞赛数据", competitions.size());

        // ===== 插入组队帖 =====
        List<TeamPost> posts = List.of(
                createPost(1L, 1L, 3L, "求2名数学建模队友，冲击国奖！",
                        "本人数学系大四，有两次数学建模参赛经验，擅长建模与算法，获得过省一等奖。今年目标冲击国奖，寻擅长编程（MATLAB/Python）或论文写作的同学一起参赛。要求认真负责、有团队精神，拒绝划水。",
                        "MATLAB,Python,算法,论文写作", "微信: wangwu_math", "2026-09-10", 2, 1, 0, now),
                createPost(2L, 2L, 1L, "ACM参赛队寻找第三名队友",
                        "我们队伍已有两人，分别是Codeforces Expert水平（1800+）和Master水平（2100+），均来自清华计算机系。现寻第三名队友，要求Codeforces 1700以上，擅长图论或动态规划方向，目标区域赛金牌。每周固定集训3次，时间充裕者优先。",
                        "C++,算法,数据结构,图论", "QQ: 123456789", "2026-10-05", 1, 2, 0, now),
                createPost(3L, 4L, 2L, "RoboMaster战队招募视觉算法成员",
                        "我校战队\"鲲鹏\"连续三年入围全国赛，今年目标是冲击八强。视觉组目前3人，还需补充2名精通计算机视觉的成员。要求：熟悉OpenCV和深度学习框架，有目标检测或图像分割项目经验，每周至少能投入15小时。",
                        "OpenCV,Python,深度学习,目标检测,ROS", "微信: rm_vision_2026", "2026-11-20", 2, 3, 0, now),
                createPost(4L, 1L, 2L, "新手求带！数学建模组队",
                        "大二软件工程专业，擅长Python编程和数据处理，有Vue.js前端开发经验，可以为队伍搭建数据可视化平台。今年第一次参加数学建模竞赛，希望找有经验的队友一起学习进步！态度认真，不拖后腿。",
                        "Python,数据分析,Vue.js,MATLAB", "微信: lisi_frontend", "2026-09-05", 2, 1, 0, now),
                createPost(5L, 9L, 1L, "互联网+项目招募：AI智能学习助手",
                        "我们的项目\"智学助手\"已进入开发阶段——基于大语言模型的个性化学习系统，能根据学生的学习情况自动定制学习计划并生成练习题。已有投资人表达意向。现有后端和算法各1人，诚招前端（React/小程序）和UI设计师各1名。",
                        "React,小程序开发,UI设计,Node.js", "邮箱: zhangsan@tsinghua.edu.cn", "2026-12-10", 2, 2, 0, now),
                createPost(6L, 3L, 2L, "电赛团队招募硬件方向同学",
                        "本人熟练STM32和嵌入式C开发，有电赛经验（去年省二等奖）。今年寻擅长模拟电路设计或FPGA开发的同学组队。要求能独立完成电路设计、PCB绘制和调试，比赛期间能全天投入。",
                        "STM32,模拟电路,PCB设计,FPGA,嵌入式C", "微信: lisi_hardware", "2026-08-25", 2, 1, 0, now),
                createPost(7L, 6L, 3L, "CTF战队招募Web安全方向",
                        "我们战队\"0xFLAG\"去年获国赛二等奖，今年目标冲击一等奖。目前Web方向人手不足，寻一名擅长Web安全的同学加入。要求：熟悉SQL注入、XSS、CSRF等常见漏洞利用，了解Java/PHP代码审计，有CTF参赛经验优先。每周有内部培训和模拟赛。",
                        "Web安全,SQL注入,代码审计,Python,BurpSuite", "QQ群: 99988877", "2026-09-01", 1, 2, 0, now),
                createPost(8L, 8L, 2L, "计算机设计大赛：校园二手交易平台",
                        "项目是一个专注于校园场景的二手交易小程序，特色功能包括：基于图像识别的商品自动分类、基于位置的就近推荐、基于信誉体系的用户信用评分。目前已完成需求分析和原型设计，寻后端开发（Spring Boot）和算法（推荐系统）各1人。",
                        "Spring Boot,小程序开发,推荐算法,MySQL", "微信: lisi_miniprogram", "2026-08-10", 2, 1, 0, now),
                createPost(9L, 12L, 1L, "ROBOCON战队招新，寻找机械和控制方向",
                        "清华大学ROBOCON战队是传统强队，多次获得全国一等奖。今年赛题已公布，机械组和控制组各需补充1人。机械方向要求熟练SolidWorks，有加工经验；控制方向要求熟悉PID和运动控制，有ROS或单片机开发经验。",
                        "SolidWorks,机械设计,PID控制,ROS,STM32", "微信: zhangsan_robot", "2026-09-20", 2, 4, 0, now),
                createPost(10L, 5L, 3L, "英语演讲比赛训练小组",
                        "大四数学系（别惊讶），曾获全国大学生英语竞赛特等奖，英语口语流利。今年尝试英语演讲比赛，想找2-3名同样备战外研社杯的同学组一个训练小组，每周1-2次模拟演讲和互相点评。不一定组队参赛（比赛是个人赛），但可以互相督促、共同进步。",
                        "英语口语,演讲技巧,辩论经验", "微信: wangwu_english", "2026-11-15", 3, 1, 0, now)
        );
        for (TeamPost p : posts) {
            teamPostMapper.insert(p);
        }
        log.info("已插入 {} 条组队帖数据", posts.size());
    }

    private User createUser(String name, String passwordHash, String school, String major,
                             String grade, String bio, String skills, LocalDateTime now) {
        User u = new User();
        u.setName(name);
        u.setPasswordHash(passwordHash);
        u.setSchool(school);
        u.setMajor(major);
        u.setGrade(grade);
        u.setBio(bio);
        u.setSkills(skills);
        u.setCreatedAt(now);
        return u;
    }

    private Competition createComp(Long id, String title, String category, String level, String organizer,
                                    String deadline, String eventDate, String location,
                                    int minTeamSize, int maxTeamSize, String description,
                                    String website, String prize, LocalDateTime now) {
        Competition c = new Competition();
        c.setId(id);
        c.setTitle(title);
        c.setCategory(category);
        c.setLevel(level);
        c.setOrganizer(organizer);
        c.setDeadline(LocalDate.parse(deadline));
        c.setEventDate(LocalDate.parse(eventDate));
        c.setLocation(location);
        c.setMinTeamSize(minTeamSize);
        c.setMaxTeamSize(maxTeamSize);
        c.setDescription(description);
        c.setWebsite(website);
        c.setPrize(prize);
        c.setCreatedAt(now);
        return c;
    }

    private TeamPost createPost(Long id, Long competitionId, Long authorId, String title, String description,
                                 String requiredSkills, String contact, String deadline,
                                 int needCount, int currentCount, int status, LocalDateTime now) {
        TeamPost p = new TeamPost();
        p.setId(id);
        p.setCompetitionId(competitionId);
        p.setAuthorId(authorId);
        p.setTitle(title);
        p.setDescription(description);
        p.setRequiredSkills(requiredSkills);
        p.setContact(contact);
        p.setDeadline(LocalDate.parse(deadline));
        p.setNeedCount(needCount);
        p.setCurrentCount(currentCount);
        p.setStatus(status);
        p.setCreatedAt(now);
        return p;
    }
}