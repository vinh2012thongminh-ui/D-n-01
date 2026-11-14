package com.vinhho.onthi10ai

import com.vinhho.onthi10ai.data.model.AppUser
import com.vinhho.onthi10ai.data.model.QuizQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun models_storePropertiesCorrectly() {
    val user = AppUser(uid = "u1", displayName = "An", email = "an@example.com", isGuest = false)
    assertEquals("u1", user.uid)
    assertEquals("an@example.com", user.email)

    val question = QuizQuestion(
      question = "1 + 1 = ?",
      options = listOf("A. 1", "B. 2", "C. 3", "D. 4"),
      correctIndex = 1,
      explanation = "1 + 1 = 2"
    )
    assertEquals(4, question.options.size)
    assertTrue(question.correctIndex in 0..3)
  }
}
