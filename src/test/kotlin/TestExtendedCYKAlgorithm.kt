import org.example.e3fxgaming.wortproblem.InputReader
import org.example.e3fxgaming.wortproblem.Solver
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.random.Random

class TestExtendedCYKAlgorithm {

    companion object {
        lateinit var solver: Solver

        @BeforeAll
        @JvmStatic
        @Suppress("Unused")
        fun setup() {
            val fileContent = TestExtendedCYKAlgorithm::class.java.getResource("/inputChomskyNF.txt")?.readText() ?: throw Exception("Rule input empty")
            val inputReader = InputReader(fileContent)
            solver = Solver(inputReader)
        }


        private val charPool = listOf('a', 'b')

        private fun randomWord(length: Int) = (1..length)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")

        @JvmStatic
        fun validWordProvider(): Stream<Arguments> {
            val numberOfWords = 30

            val strings = (1..numberOfWords).map {
                val stringLength = Random.nextInt(3, 10)

                val randomString = randomWord(stringLength)

                randomString + randomString.reversed()
            }

            return strings.map { Arguments.of(it) }.stream()
        }

        @JvmStatic
        fun invalidWordProvider(): Stream<Arguments> {
            val numberOfWords = 30

            val strings = (1..numberOfWords).map {
                val stringLength = Random.nextInt(3, 10)

                val randomString = randomWord(stringLength)
                randomString + randomString.reversed() + "a"
            }

            return strings.map { Arguments.of(it) }.stream()
        }
    }

    @ParameterizedTest
    @MethodSource("validWordProvider")
    fun testValidWord(word: String) {
        val result = solver.solve(word)
        Assertions.assertTrue(result, "Solver returned $result for word $word, but should have returned true.")
    }

    @ParameterizedTest
    @MethodSource("invalidWordProvider")
    fun testInvalidWord(word: String) {
        val result = solver.solve(word)
        Assertions.assertFalse(result, "Solver returned $result for word $word, but should have returned false.")
    }

}