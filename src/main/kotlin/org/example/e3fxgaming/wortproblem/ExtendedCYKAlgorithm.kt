package org.example.e3fxgaming.wortproblem

fun main() {
    val inputReader = InputReader()

    val firstTestWord = "abaaba"
    val secondTestWord = "abbbba"
    val thirdTestWord = "bbabbbaabbaabbbabb"
    val falseWord = "aaabbbb"

    val solver = Solver(inputReader)
    println("$firstTestWord: ${solver.solve(firstTestWord)}")
    println("$secondTestWord: ${solver.solve(secondTestWord)}")
    println("$thirdTestWord: ${solver.solve(thirdTestWord)}")
    println("$falseWord: ${solver.solve(falseWord)}")

    println(inputReader)
}

/**
 * Class for reading the Chomsky NF input. Also holds the cache.
 */
class InputReader {
    private val fileContent = InputReader::class.java.getResource("/inputChomskyNF.txt")?.readText() ?: throw Exception("No rules found")

    //Maps second part of rule to all possible first parts
    private val rules = fileContent.split("\n")
        .map { it.split("->") }
        .map {
            Rule(it[0], it[1])
        }

    val reversedRules = rules.groupBy {
        it.productionOutput
    }

    //caching rule wrappers to speed up subsequent same-word-evaluations
    val cache: MutableMap<String, DerivationWrapper> = mutableMapOf()


    override fun toString(): String {
        return "Read ${rules.size } rules. Current cache size: ${cache.size} entries."
    }
}

/**
 * An element of the production system, that can transform a [variable] into an [productionOutput]
 */
data class Rule(val variable: String, val productionOutput: String) {
    override fun toString(): String {
        return "$variable->$productionOutput"
    }
}

/**
 * Since the same [word] may be derived in multiple ways, this DerivationWrapper bundles the possible [derivations].
 * Also stores information about its direct [left] and [right] DerivationWrapper descendants.
 *
 */
data class DerivationWrapper(val word: String, val derivations: List<DerivationOperation>, val left: DerivationWrapper?, val right: DerivationWrapper?) {
    fun getChild(childSelector: (DerivationWrapper) -> DerivationWrapper?, collectionList: MutableList<DerivationWrapper>): List<DerivationWrapper> {
        collectionList.add(this)
        childSelector(this)?.getChild(childSelector, collectionList)
        return collectionList
    }

    override fun toString(): String {
        return derivations.joinToString("\n")
    }
}

/**
 * Represents a single derivation A -> A B with [rule], where A ends at [firstSegmentEndsAtIndex].
 */
data class DerivationOperation(val rule: Rule, val firstSegmentEndsAtIndex: Int) {
    override fun toString(): String {
        return "$rule ($firstSegmentEndsAtIndex)"
    }
}

/**
 * Wrapper class for solving word problems.
 * [inputReader] provides the grammar information.
 */
class Solver(private val inputReader: InputReader) {

    /**
     * Tests whether a [word] string belongs to the language of the provided grammar.
     */
    fun solve(word: String): Boolean {
        val solutionPyramid: MutableList<List<DerivationWrapper>> = mutableListOf()

        //create foundation row of solutionPyramid
        word.map { wordChar ->
            val substring = wordChar.toString()

            inputReader.cache[substring]?.let {
                return@map it
            }

            val derivations = inputReader.reversedRules[substring]?.map { rule ->
                DerivationOperation(rule, 0)
            }.orEmpty()

            DerivationWrapper(substring, derivations, null, null).also {
                inputReader.cache[it.word] = it
            }
        }.let { firstRow ->
            solutionPyramid.add(firstRow)
        }


        //create top rows of solutionPyramid
        do {
            val lastRow = solutionPyramid.last()
            val combinationPairs = (0 until lastRow.lastIndex).map { indexFirstElement ->
                val leftDerivationWrapper = lastRow[indexFirstElement]
                val rightDerivationWrapper = lastRow[indexFirstElement + 1]
                val substring = word.substring(indexFirstElement, indexFirstElement + solutionPyramid.size + 1)
                combinePairs(leftDerivationWrapper, rightDerivationWrapper, substring)
            }
            solutionPyramid.add(combinationPairs)
        } while (solutionPyramid.last().size != 1)

        return solutionPyramid.last().first().derivations.any { it.rule.variable == "S" }
    }

    /**
     * Combines a [leftDerivationWrapper] and [rightDerivationWrapper] into a superior [DerivationWrapper] for a given [word]
     */
    private fun combinePairs(leftDerivationWrapper: DerivationWrapper, rightDerivationWrapper: DerivationWrapper, word: String): DerivationWrapper {
        inputReader.cache[word]?.let {
            return it
        }

        val leftDerivationWrappers = leftDerivationWrapper.getChild({it.left}, mutableListOf())
        val rightDerivationWrappers = rightDerivationWrapper.getChild({it.right}, mutableListOf())

        if(leftDerivationWrappers.size != rightDerivationWrappers.size) throw Exception("DerivationWrapper not balanced")

        val pairs = leftDerivationWrappers.zip(rightDerivationWrappers.reversed()).filterNot {
            it.first.derivations.isEmpty() || it.second.derivations.isEmpty()
        }

        val derivationOperations = pairs.map { derivationWrappers ->
            val firstProductionInputs = derivationWrappers.first.derivations.map { it.rule.variable }
            val secondProductionInputs = derivationWrappers.second.derivations.map { it.rule.variable }

            val productionPairs = firstProductionInputs.toPairList(secondProductionInputs).map { it.joinToString(" ") }

            val applicableRules = productionPairs.mapNotNull { productionPair ->
                inputReader.reversedRules[productionPair]
            }.flatten()

            applicableRules.map { DerivationOperation(it, derivationWrappers.first.word.length) }
        }.flatten()

        return DerivationWrapper(word, derivationOperations, leftDerivationWrapper, rightDerivationWrapper).also {
            inputReader.cache[it.word] = it
        }
    }
}

/**
 * Creates the Cartesian product of the elements of this List and a different [otherList] List.
 */
private fun List<String>.toPairList(otherList: List<String>) = flatMap { aItem ->
    otherList.map { bItem ->
        listOf(aItem, bItem) }
}