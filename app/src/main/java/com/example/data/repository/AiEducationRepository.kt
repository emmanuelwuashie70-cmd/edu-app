package com.example.data.repository

import android.util.Log
import com.example.model.AiExplanationStyle
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

data class GeneratedCopilotPackage(
    val lessonNotes: String,
    val assessmentQuestions: String,
    val gradingRubric: String
)

class AiEducationRepository {

    private val tutorModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val copilotSchema = Schema.obj(
        properties = mapOf(
            "lessonNotes" to Schema.string(
                description = "Structured lesson notes with learning objectives, Ghanaian real-world hook, core explanation, and summary."
            ),
            "assessmentQuestions" to Schema.string(
                description = "3 to 5 formative and exam-style questions (mix of MCQ and short structured questions) with answers."
            ),
            "gradingRubric" to Schema.string(
                description = "Clear marking scheme and points breakdown for teachers."
            )
        )
    )

    private val copilotModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = copilotSchema
            }
        )
    }

    fun askTutorStream(
        subjectTitle: String,
        levelLabel: String,
        topic: String,
        style: AiExplanationStyle,
        question: String
    ): Flow<String> = flow {
        val prompt = """
            You are Adesua AI Tutor, an authoritative, encouraging Ghanaian academic mentor.
            Context:
            - Subject/Course: $subjectTitle
            - Student Level: $levelLabel
            - Topic Focus: ${topic.ifBlank { "General curriculum guidance" }}
            - Explanation Style: ${style.label} — ${style.instruction}

            Student Question:
            $question

            Respond clearly using Markdown-free clean headings and bullet points suitable for mobile reading.
        """.trimIndent()

        val accumulated = StringBuilder()
        try {
            withTimeout(60_000L) {
                tutorModel.generateContentStream(prompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        emit(accumulated.toString())
                    }
                }
            }
            if (accumulated.isBlank()) {
                throw IllegalStateException("AI Tutor returned an empty response.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI Tutor request timed out", e)
            throw IllegalStateException("AI Tutor timed out after 60 seconds. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI Tutor streaming failed", e)
            throw e
        }
    }

    suspend fun generateTeacherCopilotPackage(
        unitTitle: String,
        strandOrTopic: String,
        gradeOrLevel: String,
        durationMinutes: Int
    ): GeneratedCopilotPackage {
        val prompt = """
            You are Adesua AI Teacher Copilot supporting a Ghanaian educator.
            Generate a complete, ready-to-teach lesson package aligned with Ghanaian NaCCA / tertiary academic standards.
            - Subject/Course: $unitTitle
            - Strand / Topic: $strandOrTopic
            - Target Level: $gradeOrLevel
            - Lesson Duration: $durationMinutes minutes

            Include:
            1. lessonNotes: Starter activity with a Ghanaian context, core concept breakdown, worked examples, and plenary recap.
            2. assessmentQuestions: 4 practice questions (2 MCQs + 2 structured WASSCE/university-style questions) with model answers.
            3. gradingRubric: Point-by-point marking rubric and differentiation tips.
        """.trimIndent()

        try {
            val response = withTimeout(60_000L) {
                copilotModel.generateContent(prompt)
            }
            val rawJson = response.text
                ?: throw IllegalStateException("AI Teacher Copilot returned an empty response.")
            val obj = JSONObject(rawJson)
            return GeneratedCopilotPackage(
                lessonNotes = obj.getString("lessonNotes"),
                assessmentQuestions = obj.getString("assessmentQuestions"),
                gradingRubric = obj.getString("gradingRubric")
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Teacher Copilot timed out", e)
            throw IllegalStateException("Lesson generation timed out after 60 seconds. Please try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Teacher Copilot generation failed", e)
            throw e
        }
    }

    private companion object {
        const val TAG = "AiEducationRepo"
    }
}
