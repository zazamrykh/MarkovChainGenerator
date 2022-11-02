class MarkovGenerator {

    fun <T> generateRandomList(sequentialDataList : MutableList<T>): MutableList<T> {
        // Hello everybody to Markov Generator!
        // I will show you how to generate random text using Markov chain! LETS'GO!!!!!

        // Make Markov matrix! Firstly it will keep number of usages next word after current, but then we will divide
        // Every element in a row at number of all usages of next word.
        val transitionMatrix = SquareMatrix(1)

        // Make dictionary that will keep elements, their index and quantity.
        val dictionary = mutableMapOf<T, Pair<Int, Int>>()

        // Directly algorithm of drafting dictionary and Markov matrix.
        var indexOfPreviousElement = -1
        var indexOfCurrentElement: Int
        var isFirstElement = true
        var isRepeatedElement: Boolean
        for (element in sequentialDataList) {
            if (dictionary.containsKey(element)) {
                indexOfCurrentElement = dictionary.getValue(element).first
                dictionary[element] = dictionary[element]?.copy(second = dictionary[element]!!.second + 1)
                    ?: throw NoSuchElementException()
                isRepeatedElement = true
            } else {
                indexOfCurrentElement = dictionary.size
                dictionary[element] = Pair(indexOfCurrentElement, 1)
                isRepeatedElement = false
            }

            if (isFirstElement) {
                indexOfPreviousElement = indexOfCurrentElement
                isFirstElement = false
                continue
            }

            if (isRepeatedElement) {
                transitionMatrix.setElementAt(
                    transitionMatrix
                        .getElementAt(indexOfPreviousElement, indexOfCurrentElement) + 1,
                    indexOfPreviousElement, indexOfCurrentElement
                )
            } else {
                transitionMatrix.expandMatrix()
                transitionMatrix.setElementAt(
                    1.0,
                    indexOfPreviousElement, indexOfCurrentElement
                )
            }
            indexOfPreviousElement = indexOfCurrentElement
        }

        // Divide every row of matrix at number of usages, so we will get probabilities matrix.
        val matrixSize = dictionary.size
        var numberOfAppearance: Int
        var indexOfWord: Int
        for (dic in dictionary) {
            numberOfAppearance = dic.value.second
            indexOfWord = dic.value.first
            for (j in 0 until matrixSize) {
                if (numberOfAppearance == 0) {
                    transitionMatrix.setElementAt(
                        -1.0,
                        indexOfWord, j
                    )
                } else {
                    transitionMatrix.setElementAt(
                        transitionMatrix.getElementAt(indexOfWord, j) / numberOfAppearance,
                        indexOfWord, j
                    )
                }
            }
        }

        // Generation algorithm
        val generatedText = mutableListOf<T>()
        var wordIndex = 0
        var randomElement: T
        generatedText.add(getElementUsingIndex(dictionary, wordIndex)?:throw NoSuchElementException())
        while (true) {
            wordIndex = getRandomIndexOfWordAfter(transitionMatrix, wordIndex)
            if (wordIndex == -1) {
                break
            }
            randomElement = getElementUsingIndex(dictionary, wordIndex)?: throw NoSuchElementException()
            generatedText.add(randomElement)
        }
        return generatedText
    }

    private fun <T> getElementUsingIndex(dictionary: MutableMap<T, Pair<Int, Int>>, index: Int): T? {
        for (dic in dictionary) {
            if (index == dic.value.first) {
                return dic.key
            }
        }
        return null
    }

    private fun getRandomIndexOfWordAfter(probabilities: SquareMatrix, wordIndex: Int): Int {
        val rowOfProbabilityNextWord: MutableList<Double> = probabilities.getRow(wordIndex)
        var randomValue = getRandomValueFromZeroToOne()
        for (indexOfNextWord in 0 until rowOfProbabilityNextWord.size) {
            if (rowOfProbabilityNextWord[indexOfNextWord] < 0) {
                return -1
            }
            randomValue -= rowOfProbabilityNextWord[indexOfNextWord]
            if (randomValue < 0) {
                return indexOfNextWord
            }
        }
        if (randomValue > 0) {
            return -1
        }
        return rowOfProbabilityNextWord.size - 1
    }

    private fun getRandomValueFromZeroToOne(): Double {
        return ((0..10000).shuffled().last().toDouble() / 10000)
    }

}