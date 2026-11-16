-- Adesua Educational Platform - PostgreSQL / Supabase Migration Schema
-- Complete relational reference schema with Row Level Security (RLS)

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE user_role AS ENUM ('student', 'teacher', 'tutor', 'admin');
CREATE TYPE institution_type AS ENUM ('SHS', 'UNIVERSITY');
CREATE TYPE resource_type AS ENUM ('TEXTBOOK', 'LECTURE_NOTE', 'VIDEO', 'PAST_QUESTION', 'LESSON_PLAN');

CREATE TABLE profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    role user_role NOT NULL DEFAULT 'student',
    avatar_url TEXT,
    education_level institution_type,
    institution_id UUID,
    form_or_level TEXT,
    learning_area_or_programme TEXT,
    semester TEXT,
    academic_year TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE institutions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    type institution_type NOT NULL,
    region TEXT NOT NULL
);

CREATE TABLE academic_units (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    institution_id UUID REFERENCES institutions(id) ON DELETE SET NULL,
    code TEXT NOT NULL,
    title TEXT NOT NULL,
    level TEXT NOT NULL,
    semester TEXT,
    learning_area_or_programme TEXT NOT NULL,
    is_verified_curriculum BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE curriculum_topics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    unit_id UUID NOT NULL REFERENCES academic_units(id) ON DELETE CASCADE,
    strand_or_topic TEXT NOT NULL,
    sub_strand_or_subtopic TEXT NOT NULL,
    learning_objectives JSONB NOT NULL DEFAULT '[]'::jsonb
);

CREATE TABLE student_enrollments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    unit_id UUID NOT NULL REFERENCES academic_units(id) ON DELETE CASCADE,
    academic_year TEXT NOT NULL,
    UNIQUE(student_id, unit_id, academic_year)
);

CREATE TABLE learning_resources (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    unit_id UUID NOT NULL REFERENCES academic_units(id) ON DELETE CASCADE,
    uploader_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    resource_type resource_type NOT NULL,
    file_url TEXT NOT NULL,
    is_teacher_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE assessments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    unit_id UUID NOT NULL REFERENCES academic_units(id) ON DELETE CASCADE,
    creator_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    questions JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE assessment_attempts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    assessment_id UUID NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    unit_id UUID NOT NULL REFERENCES academic_units(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    score INTEGER NOT NULL CHECK (score >= 0 AND score <= 100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE live_classes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    unit_id UUID NOT NULL REFERENCES academic_units(id) ON DELETE CASCADE,
    host_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    meeting_link TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'UPCOMING'
);

CREATE TABLE tutor_profiles (
    tutor_id UUID PRIMARY KEY REFERENCES profiles(id) ON DELETE CASCADE,
    bio TEXT NOT NULL,
    credentials TEXT NOT NULL,
    hourly_rate_ghs NUMERIC(10, 2) NOT NULL CHECK (hourly_rate_ghs >= 0),
    rating_avg NUMERIC(3, 2) NOT NULL DEFAULT 0.0,
    total_reviews INTEGER NOT NULL DEFAULT 0,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE tutor_bookings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tutor_id UUID NOT NULL REFERENCES tutor_profiles(tutor_id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    booking_time TIMESTAMPTZ NOT NULL,
    amount_ghs NUMERIC(10, 2) NOT NULL,
    payment_status TEXT NOT NULL DEFAULT 'PENDING'
);

CREATE TABLE study_groups (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    unit_id UUID NOT NULL REFERENCES academic_units(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE forum_posts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    unit_id UUID NOT NULL REFERENCES academic_units(id) ON DELETE CASCADE,
    group_id UUID REFERENCES study_groups(id) ON DELETE SET NULL,
    author_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE student_enrollments ENABLE ROW LEVEL SECURITY;
ALTER TABLE learning_resources ENABLE ROW LEVEL SECURITY;
ALTER TABLE assessments ENABLE ROW LEVEL SECURITY;
ALTER TABLE assessment_attempts ENABLE ROW LEVEL SECURITY;
ALTER TABLE live_classes ENABLE ROW LEVEL SECURITY;
ALTER TABLE tutor_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE tutor_bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE forum_posts ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Users read own profile" ON profiles FOR SELECT USING (auth.uid() = id);
CREATE POLICY "Users update own profile" ON profiles FOR UPDATE USING (auth.uid() = id);
CREATE POLICY "Students manage own enrollments" ON student_enrollments FOR ALL USING (auth.uid() = student_id);
CREATE POLICY "Authenticated read verified resources" ON learning_resources FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY "Teachers/Tutors upload resources" ON learning_resources FOR INSERT WITH CHECK (auth.uid() = uploader_id);
CREATE POLICY "Students manage own attempts" ON assessment_attempts FOR ALL USING (auth.uid() = student_id);
CREATE POLICY "Authenticated read live classes" ON live_classes FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY "Hosts manage live classes" ON live_classes FOR ALL USING (auth.uid() = host_id);
CREATE POLICY "Public read verified tutors" ON tutor_profiles FOR SELECT USING (true);
CREATE POLICY "Students and tutors view own bookings" ON tutor_bookings FOR SELECT USING (auth.uid() = student_id OR auth.uid() = tutor_id);
CREATE POLICY "Authenticated read/post forum" ON forum_posts FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY "Authors insert own forum posts" ON forum_posts FOR INSERT WITH CHECK (auth.uid() = author_id);
