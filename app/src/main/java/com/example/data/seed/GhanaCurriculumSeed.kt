package com.example.data.seed

import com.example.model.AcademicUnit
import com.example.model.CurriculumTopic
import com.example.model.EducationLevel
import com.example.model.ForumPost
import com.example.model.Institution
import com.example.model.LearningResource
import com.example.model.LiveClass
import com.example.model.LiveClassStatus
import com.example.model.McqQuestion
import com.example.model.PracticeAssessment
import com.example.model.ResourceType
import com.example.model.TutorProfile
import com.example.model.UserRole

object GhanaCurriculumSeed {

    val shsSchools: List<Institution> = listOf(
        Institution("wghs", "Wesley Girls' High School", EducationLevel.SHS, "Central Region"),
        Institution("prempeh", "Prempeh College", EducationLevel.SHS, "Ashanti Region"),
        Institution("achimota", "Achimota School", EducationLevel.SHS, "Greater Accra"),
        Institution("presec", "PRESEC Legon", EducationLevel.SHS, "Greater Accra"),
        Institution("abugiss", "Aburi Girls' Senior High", EducationLevel.SHS, "Eastern Region"),
        Institution("owass", "Opoku Ware School", EducationLevel.SHS, "Ashanti Region"),
        Institution("mfantsipim", "Mfantsipim School", EducationLevel.SHS, "Central Region")
    )

    val universities: List<Institution> = listOf(
        Institution("ug", "University of Ghana (Legon)", EducationLevel.UNIVERSITY, "Greater Accra"),
        Institution("knust", "KNUST (Kumasi)", EducationLevel.UNIVERSITY, "Ashanti Region"),
        Institution("ucc", "University of Cape Coast (UCC)", EducationLevel.UNIVERSITY, "Central Region"),
        Institution("uew", "University of Education, Winneba", EducationLevel.UNIVERSITY, "Central Region"),
        Institution("ashesi", "Ashesi University", EducationLevel.UNIVERSITY, "Eastern Region")
    )

    val shsForms = listOf("SHS 1", "SHS 2", "SHS 3")
    val shsLearningAreas = listOf("General Science", "Business", "General Arts", "Visual Arts")
    val universityLevels = listOf("Level 100", "Level 200", "Level 300", "Level 400")
    val universitySemesters = listOf("First Semester", "Second Semester")
    val universityProgrammes = listOf(
        "BSc Computer Science",
        "BSc Business Administration",
        "BA Economics & Political Science",
        "BSc Biological Sciences"
    )

    val shsCoreUnits: List<AcademicUnit> = listOf(
        AcademicUnit(
            id = "shs_core_math",
            institutionId = null,
            code = "MTH-CORE",
            title = "Core Mathematics",
            level = "SHS 1-3",
            semester = null,
            learningAreaOrProgramme = "NaCCA Core",
            isCore = true,
            isVerifiedCurriculum = true
        ),
        AcademicUnit(
            id = "shs_eng",
            institutionId = null,
            code = "ENG-CORE",
            title = "English Language",
            level = "SHS 1-3",
            semester = null,
            learningAreaOrProgramme = "NaCCA Core",
            isCore = true,
            isVerifiedCurriculum = true
        ),
        AcademicUnit(
            id = "shs_int_sci",
            institutionId = null,
            code = "SCI-CORE",
            title = "Integrated Science",
            level = "SHS 1-3",
            semester = null,
            learningAreaOrProgramme = "NaCCA Core",
            isCore = true,
            isVerifiedCurriculum = true
        ),
        AcademicUnit(
            id = "shs_soc_stud",
            institutionId = null,
            code = "SOC-CORE",
            title = "Social Studies",
            level = "SHS 1-3",
            semester = null,
            learningAreaOrProgramme = "NaCCA Core",
            isCore = true,
            isVerifiedCurriculum = true
        )
    )

    val shsElectiveUnits: List<AcademicUnit> = listOf(
        // General Science
        AcademicUnit("shs_e_math", null, "EMTH-SHS", "Elective Mathematics", "SHS 1-3", null, "General Science"),
        AcademicUnit("shs_physics", null, "PHY-SHS", "Physics", "SHS 1-3", null, "General Science"),
        AcademicUnit("shs_chem", null, "CHM-SHS", "Chemistry", "SHS 1-3", null, "General Science"),
        AcademicUnit("shs_bio", null, "BIO-SHS", "Biology", "SHS 1-3", null, "General Science"),
        // Business
        AcademicUnit("shs_fin_acc", null, "ACC-SHS", "Financial Accounting", "SHS 1-3", null, "Business"),
        AcademicUnit("shs_bus_mgt", null, "BMG-SHS", "Business Management", "SHS 1-3", null, "Business"),
        AcademicUnit("shs_econ_bus", null, "ECN-SHS", "Economics", "SHS 1-3", null, "Business"),
        AcademicUnit("shs_cost_acc", null, "CST-SHS", "Cost Accounting", "SHS 1-3", null, "Business"),
        // General Arts
        AcademicUnit("shs_lit", null, "LIT-SHS", "Literature-in-English", "SHS 1-3", null, "General Arts"),
        AcademicUnit("shs_govt", null, "GOV-SHS", "Government", "SHS 1-3", null, "General Arts"),
        AcademicUnit("shs_hist", null, "HIS-SHS", "History of Ghana & Africa", "SHS 1-3", null, "General Arts"),
        AcademicUnit("shs_econ_arts", null, "ECN-ARTS", "Economics", "SHS 1-3", null, "General Arts"),
        // Visual Arts
        AcademicUnit("shs_gka", null, "GKA-SHS", "General Knowledge in Art", "SHS 1-3", null, "Visual Arts"),
        AcademicUnit("shs_graphic", null, "GRD-SHS", "Graphic Design", "SHS 1-3", null, "Visual Arts"),
        AcademicUnit("shs_picture", null, "PIC-SHS", "Picture Making", "SHS 1-3", null, "Visual Arts"),
        AcademicUnit("shs_textiles", null, "TXT-SHS", "Textiles & Kente Weaving", "SHS 1-3", null, "Visual Arts")
    )

    // Illustrative University Curriculum (explicitly marked isVerifiedCurriculum = false)
    val universityIllustrativeUnits: List<AcademicUnit> = listOf(
        AcademicUnit("uni_csc101", "illustrative", "ILL-CSC101", "Introduction to Computing & Algorithms", "Level 100", "First Semester", "BSc Computer Science", isVerifiedCurriculum = false),
        AcademicUnit("uni_csc204", "illustrative", "ILL-CSC204", "Data Structures & Complexity", "Level 200", "First Semester", "BSc Computer Science", isVerifiedCurriculum = false),
        AcademicUnit("uni_csc301", "illustrative", "ILL-CSC301", "Relational Database Systems", "Level 300", "Second Semester", "BSc Computer Science", isVerifiedCurriculum = false),
        AcademicUnit("uni_csc405", "illustrative", "ILL-CSC405", "Distributed Systems & Cloud Architecture", "Level 400", "First Semester", "BSc Computer Science", isVerifiedCurriculum = false),
        AcademicUnit("uni_bus102", "illustrative", "ILL-BUS102", "Principles of Management", "Level 100", "First Semester", "BSc Business Administration", isVerifiedCurriculum = false),
        AcademicUnit("uni_acc201", "illustrative", "ILL-ACC201", "Intermediate Financial Reporting (IFRS)", "Level 200", "Second Semester", "BSc Business Administration", isVerifiedCurriculum = false),
        AcademicUnit("uni_fin301", "illustrative", "ILL-FIN301", "Ghanaian Capital Markets & Corporate Finance", "Level 300", "First Semester", "BSc Business Administration", isVerifiedCurriculum = false),
        AcademicUnit("uni_ecn101", "illustrative", "ILL-ECN101", "Principles of Microeconomics", "Level 100", "First Semester", "BA Economics & Political Science", isVerifiedCurriculum = false),
        AcademicUnit("uni_pol202", "illustrative", "ILL-POL202", "Constitutional Governance in Ghana", "Level 200", "Second Semester", "BA Economics & Political Science", isVerifiedCurriculum = false),
        AcademicUnit("uni_bio101", "illustrative", "ILL-BIO101", "Cell Biology & Genetics", "Level 100", "First Semester", "BSc Biological Sciences", isVerifiedCurriculum = false),
        AcademicUnit("uni_bio205", "illustrative", "ILL-BIO205", "Tropical Ecology & Conservation", "Level 200", "Second Semester", "BSc Biological Sciences", isVerifiedCurriculum = false)
    )

    val allAcademicUnits: List<AcademicUnit> =
        shsCoreUnits + shsElectiveUnits + universityIllustrativeUnits

    fun unitsForShsArea(learningArea: String): List<AcademicUnit> =
        shsCoreUnits + shsElectiveUnits.filter { it.learningAreaOrProgramme == learningArea }

    fun unitsForUniversityProgramme(programme: String): List<AcademicUnit> =
        universityIllustrativeUnits.filter { it.learningAreaOrProgramme == programme }
            .ifEmpty { universityIllustrativeUnits.take(3) }

    val topicsByUnitId: Map<String, List<CurriculumTopic>> = mapOf(
        "shs_core_math" to listOf(
            CurriculumTopic("t_math_1", "shs_core_math", "Algebra & Number", "Quadratic Equations & Surds", listOf("Factorise quadratic expressions", "Apply the quadratic formula to real problems")),
            CurriculumTopic("t_math_2", "shs_core_math", "Geometry & Trigonometry", "Circle Theorems & Bearings", listOf("Prove angle at centre is twice angle at circumference", "Solve 3-figure bearing problems"))
        ),
        "shs_int_sci" to listOf(
            CurriculumTopic("t_sci_1", "shs_int_sci", "Cycles & Energy", "Photosynthesis & Soil Conservation", listOf("Explain light and dark reactions", "Evaluate Ghanaian crop rotation practices")),
            CurriculumTopic("t_sci_2", "shs_int_sci", "Matter & Forces", "Acids, Bases & Salts", listOf("Determine pH using universal indicator", "Prepare soluble salts by titration"))
        ),
        "shs_bio" to listOf(
            CurriculumTopic("t_bio_1", "shs_bio", "Cell & Molecular Biology", "Osmosis, Diffusion & Active Transport", listOf("Compare turgidity and plasmolysis in plant cells")),
            CurriculumTopic("t_bio_2", "shs_bio", "Genetics", "Mendelian Inheritance & Blood Groups", listOf("Predict monohybrid cross ratios"))
        ),
        "shs_physics" to listOf(
            CurriculumTopic("t_phy_1", "shs_physics", "Mechanics", "Projectile Motion & Newton's Laws", listOf("Resolve velocity into horizontal and vertical components"))
        ),
        "shs_fin_acc" to listOf(
            CurriculumTopic("t_acc_1", "shs_fin_acc", "Final Accounts", "Partnership & Sole Trader Adjustments", listOf("Prepare Income Statement with accruals and prepayments"))
        ),
        "uni_csc204" to listOf(
            CurriculumTopic("t_csc_1", "uni_csc204", "Linear & Tree Structures", "Balanced Trees & Graph Traversal", listOf("Compare BFS and DFS time complexities O(V + E)"))
        )
    )

    val seedResources: List<LearningResource> = listOf(
        LearningResource(
            id = "seed_res_1",
            authorId = "seed_teacher_1",
            authorName = "Mr. Kwabena Boateng (PRESEC)",
            unitId = "shs_core_math",
            unitCode = "MTH-CORE",
            title = "WASSCE Core Maths Circle Theorems Master Pack",
            summary = "Annotated proofs, rider diagrams, and 15 past WAEC Section B worked solutions.",
            resourceType = ResourceType.PAST_QUESTION.apiValue,
            isTeacherVerified = true
        ),
        LearningResource(
            id = "seed_res_2",
            authorId = "seed_teacher_2",
            authorName = "Mrs. Abena Osei-Mensah (WGHS)",
            unitId = "shs_bio",
            unitCode = "BIO-SHS",
            title = "NaCCA Strand 2: Mammalian Transport & Immunity",
            summary = "Structured colour revision guide aligned with NaCCA SHS 2 Biology outcomes.",
            resourceType = ResourceType.LECTURE_NOTE.apiValue,
            isTeacherVerified = true
        ),
        LearningResource(
            id = "seed_res_3",
            authorId = "seed_teacher_3",
            authorName = "Dr. Kofi Mensah",
            unitId = "uni_csc204",
            unitCode = "ILL-CSC204",
            title = "Illustrative Lab: Graphs, Heaps & Big-O Analysis",
            summary = "Illustrative university tutorial sheet with Kotlin/Python pseudo-code walkthroughs.",
            resourceType = ResourceType.LECTURE_NOTE.apiValue,
            isTeacherVerified = false
        ),
        LearningResource(
            id = "seed_res_4",
            authorId = "seed_teacher_4",
            authorName = "Mr. Yaw Darko (Achimota)",
            unitId = "shs_eng",
            unitCode = "ENG-CORE",
            title = "WASSCE Essay & Comprehension Marking Scheme Guide",
            summary = "Examiner tips on Content, Organisation, Expression, and Mechanical Accuracy.",
            resourceType = ResourceType.TEXTBOOK.apiValue,
            isTeacherVerified = true
        ),
        LearningResource(
            id = "seed_res_5",
            authorId = "seed_teacher_5",
            authorName = "Ms. Efua Appiah (Mfantsipim)",
            unitId = "shs_fin_acc",
            unitCode = "ACC-SHS",
            title = "Bank Reconciliation & Control Accounts Drill",
            summary = "Step-by-step ledger templates for SHS Business Form 2 & Form 3 learners.",
            resourceType = ResourceType.PAST_QUESTION.apiValue,
            isTeacherVerified = true
        )
    )

    val seedAssessments: List<PracticeAssessment> = listOf(
        PracticeAssessment(
            id = "quiz_core_math",
            unitId = "shs_core_math",
            unitCode = "MTH-CORE",
            title = "WASSCE Core Maths: Algebra & Geometry",
            durationMinutes = 10,
            questions = listOf(
                McqQuestion(
                    id = "q1",
                    prompt = "If 2x^2 - 5x - 3 = 0, what are the roots of the equation?",
                    options = listOf("x = -1/2 or x = 3", "x = 1/2 or x = -3", "x = 2 or x = -3", "x = -1/2 or x = -3"),
                    correctIndex = 0,
                    explanation = "Factorising gives (2x + 1)(x - 3) = 0, so x = -1/2 or x = 3."
                ),
                McqQuestion(
                    id = "q2",
                    prompt = "An angle subtended by an arc at the centre of a circle is 116 degrees. What is the angle subtended by the same arc at the circumference?",
                    options = listOf("232 degrees", "64 degrees", "58 degrees", "116 degrees"),
                    correctIndex = 2,
                    explanation = "By the Circle Theorem, angle at the circumference is half the angle at the centre: 116 / 2 = 58 degrees."
                ),
                McqQuestion(
                    id = "q3",
                    prompt = "Simplify (sqrt(48) + sqrt(12)) / sqrt(3).",
                    options = listOf("4", "6", "3 * sqrt(3)", "8"),
                    correctIndex = 1,
                    explanation = "sqrt(48) = 4*sqrt(3) and sqrt(12) = 2*sqrt(3). Sum = 6*sqrt(3), divided by sqrt(3) = 6."
                ),
                McqQuestion(
                    id = "q4",
                    prompt = "A trader in Makola Market buys an item for GHS 240 and sells it at a 15% profit. What is the selling price?",
                    options = listOf("GHS 255", "GHS 276", "GHS 280", "GHS 264"),
                    correctIndex = 1,
                    explanation = "15% of 240 = 0.15 * 240 = GHS 36. Selling price = 240 + 36 = GHS 276."
                )
            )
        ),
        PracticeAssessment(
            id = "quiz_bio_wassce",
            unitId = "shs_bio",
            unitCode = "BIO-SHS",
            title = "SHS Biology: Cell Physiology & Genetics",
            durationMinutes = 8,
            questions = listOf(
                McqQuestion(
                    id = "bq1",
                    prompt = "Which organelle is primarily responsible for aerobic ATP synthesis in eukaryotic cells?",
                    options = listOf("Golgi apparatus", "Mitochondrion", "Ribosome", "Lysosome"),
                    correctIndex = 1,
                    explanation = "Mitochondria carry out the Krebs cycle and oxidative phosphorylation to generate ATP."
                ),
                McqQuestion(
                    id = "bq2",
                    prompt = "When a plant cell is placed in a hypertonic sucrose solution, what occurs?",
                    options = listOf("Turgidity", "Haemolysis", "Plasmolysis", "Pinocytosis"),
                    correctIndex = 2,
                    explanation = "Water leaves the vacuole by osmosis, causing the protoplasm to shrink away from the cell wall (plasmolysis)."
                ),
                McqQuestion(
                    id = "bq3",
                    prompt = "In a monohybrid cross between two heterozygous tall pea plants (Tt x Tt), what is the expected phenotypic ratio?",
                    options = listOf("1 tall : 1 dwarf", "3 tall : 1 dwarf", "1 : 2 : 1", "All tall"),
                    correctIndex = 1,
                    explanation = "Genotypes are 1 TT : 2 Tt : 1 tt, giving 3 tall to 1 dwarf."
                )
            )
        ),
        PracticeAssessment(
            id = "quiz_csc_uni",
            unitId = "uni_csc204",
            unitCode = "ILL-CSC204",
            title = "Illustrative CS Quiz: Algorithms & Complexity",
            durationMinutes = 10,
            questions = listOf(
                McqQuestion(
                    id = "cq1",
                    prompt = "What is the worst-case time complexity of binary search on a sorted array of n elements?",
                    options = listOf("O(n)", "O(log n)", "O(n log n)", "O(1)"),
                    correctIndex = 1,
                    explanation = "Each comparison halves the remaining search interval, yielding O(log n)."
                ),
                McqQuestion(
                    id = "cq2",
                    prompt = "Which data structure guarantees FIFO (First-In, First-Out) ordering, commonly used in Breadth-First Search?",
                    options = listOf("Stack", "Binary Heap", "Queue", "Hash Set"),
                    correctIndex = 2,
                    explanation = "BFS visits vertices level by level using a FIFO Queue."
                ),
                McqQuestion(
                    id = "cq3",
                    prompt = "In a min-heap with n elements, what is the time complexity of extracting the minimum element?",
                    options = listOf("O(1)", "O(log n)", "O(n)", "O(n^2)"),
                    correctIndex = 1,
                    explanation = "Replacing the root and sifting down takes O(log n) swaps proportional to heap height."
                )
            )
        )
    )

    val seedLiveClasses: List<LiveClass> = listOf(
        LiveClass(
            id = "seed_live_1",
            authorId = "seed_teacher_1",
            hostName = "Mr. Kwabena Boateng",
            unitId = "shs_core_math",
            unitCode = "MTH-CORE",
            title = "WASSCE Core Maths: Trigonometry & Bearings Clinic",
            scheduledAtLabel = "Today • 17:30 GMT",
            meetingLink = "https://meet.adesua.gh/mth-core-clinic",
            status = LiveClassStatus.LIVE.apiValue
        ),
        LiveClass(
            id = "seed_live_2",
            authorId = "seed_teacher_2",
            hostName = "Mrs. Abena Osei-Mensah",
            unitId = "shs_bio",
            unitCode = "BIO-SHS",
            title = "Practical Microscopy & WAEC Specimen Prep",
            scheduledAtLabel = "Tomorrow • 16:00 GMT",
            meetingLink = "https://meet.adesua.gh/bio-specimen-lab",
            status = LiveClassStatus.UPCOMING.apiValue
        ),
        LiveClass(
            id = "seed_live_3",
            authorId = "seed_teacher_3",
            hostName = "Dr. Kofi Mensah",
            unitId = "uni_csc204",
            unitCode = "ILL-CSC204",
            title = "Dynamic Programming & Graph Algorithms Office Hours",
            scheduledAtLabel = "Fri • 19:00 GMT",
            meetingLink = "https://meet.adesua.gh/csc204-office-hours",
            status = LiveClassStatus.UPCOMING.apiValue
        )
    )

    val seedTutors: List<TutorProfile> = listOf(
        TutorProfile(
            id = "tutor_kwame",
            fullName = "Dr. Kwame Boateng",
            headline = "WASSCE Elective Maths & Physics Specialist",
            credentials = "PhD Physics (KNUST), 9 yrs SHS Chief Examiner Prep",
            institutionExpertise = "Prempeh College & KNUST",
            subjects = listOf("Core Mathematics", "Elective Mathematics", "Physics"),
            hourlyRateGhs = 85.0,
            ratingAvg = 4.9,
            totalReviews = 114,
            isVerified = true,
            availability = "Mon-Sat • 16:00 - 21:00 GMT",
            cityOrMode = "Kumasi & Online"
        ),
        TutorProfile(
            id = "tutor_akosua",
            fullName = "Akosua Sarpong-Kumah",
            headline = "BSc Computer Science & Data Structures Mentor",
            credentials = "MSc Computer Science (UG Legon), Senior Software Engineer",
            institutionExpertise = "University of Ghana & Ashesi",
            subjects = listOf("Introduction to Computing & Algorithms", "Data Structures & Complexity", "Relational Database Systems"),
            hourlyRateGhs = 120.0,
            ratingAvg = 4.95,
            totalReviews = 68,
            isVerified = true,
            availability = "Tue-Sun • 18:00 - 22:00 GMT",
            cityOrMode = "Accra (East Legon) & Online"
        ),
        TutorProfile(
            id = "tutor_ebo",
            fullName = "Ebo Quansah, CA",
            headline = "SHS Financial Accounting & Corporate Finance Tutor",
            credentials = "Chartered Accountant (ICAG), BCom (UCC)",
            institutionExpertise = "Wesley Girls', Mfantsipim & UCC",
            subjects = listOf("Financial Accounting", "Cost Accounting", "Intermediate Financial Reporting (IFRS)"),
            hourlyRateGhs = 75.0,
            ratingAvg = 4.8,
            totalReviews = 92,
            isVerified = true,
            availability = "Weekdays 17:00+ & Weekends",
            cityOrMode = "Cape Coast & Online"
        ),
        TutorProfile(
            id = "tutor_maame",
            fullName = "Maame Efua Dadzie",
            headline = "Integrated Science, Biology & Chemistry Coach",
            credentials = "MPhil Biochemistry (UCC), NTC Licensed Educator",
            institutionExpertise = "Aburi Girls' & Wesley Girls'",
            subjects = listOf("Biology", "Chemistry", "Integrated Science"),
            hourlyRateGhs = 70.0,
            ratingAvg = 4.85,
            totalReviews = 81,
            isVerified = true,
            availability = "Mon-Fri • 15:30 - 20:00 GMT",
            cityOrMode = "Online Nationwide"
        )
    )

    val seedForumPosts: List<ForumPost> = listOf(
        ForumPost(
            id = "seed_post_1",
            authorId = "seed_student_1",
            authorName = "Kofi Adjei (SHS 3 • PRESEC)",
            authorRole = UserRole.STUDENT.apiValue,
            unitId = "shs_core_math",
            unitCode = "MTH-CORE",
            content = "Tip for Circle Theorems: always trace the radii first to spot hidden isosceles triangles before computing exterior angles!"
        ),
        ForumPost(
            id = "seed_post_2",
            authorId = "seed_teacher_2",
            authorName = "Mrs. Abena Osei-Mensah",
            authorRole = UserRole.TEACHER.apiValue,
            unitId = "shs_bio",
            unitCode = "BIO-SHS",
            content = "Remember for WAEC Biology Paper 3: biological drawings must use sharp, continuous single lines with no shading and horizontal label guidelines."
        ),
        ForumPost(
            id = "seed_post_3",
            authorId = "seed_student_2",
            authorName = "Nana Yaa Boadu (Level 200)",
            authorRole = UserRole.STUDENT.apiValue,
            unitId = "uni_csc204",
            unitCode = "ILL-CSC204",
            content = "Anyone working through Dijkstra vs Bellman-Ford complexity proofs tonight? Let's compare notes before Friday's office hours."
        )
    )
}
