package com.saintmarina.alphatraining

class FilterCoefficients {
    companion object {

    val allWaves: Array<Pair<DoubleArray, DoubleArray>> =
        arrayOf(Pair(doubleArrayOf(0.00812820697591528, 0.01625641395183056, 0.00812820697591528),
                     doubleArrayOf(1.0, -0.6705441277216879, 0.20444397427800584)),
                Pair(doubleArrayOf(1.0, 2.0, 1.0),
                     doubleArrayOf(1.0, -0.8621409589018354, 0.600422307222992)),
                Pair(doubleArrayOf(1.0, 0.0, -1.0),
                     doubleArrayOf(1.0, -1.286307270411458, 0.30420455805283164)),
                Pair(doubleArrayOf(1.0, -2.0, 1.0),
                     doubleArrayOf(1.0, -1.9590017964554152, 0.9596586667647369)),
                Pair(doubleArrayOf(1.0, -2.0, 1.0),
                     doubleArrayOf(1.0, -1.9845244803044835, 0.9851564909338199))
        )

    val alphaWaves: Array<Pair<DoubleArray, DoubleArray>> =
        arrayOf(Pair(doubleArrayOf(2.7384130003717186e-07, 5.4768260007434371e-07, 2.7384130003717186e-07),
                     doubleArrayOf(1.0000000000000000e+00,-1.8467120136656892e+00, 9.0420359373902093e-01)),
                Pair(doubleArrayOf(1.0000000000000000e+00, 2.0000000000000000e+00, 1.0000000000000000e+00),
                     doubleArrayOf(1.0000000000000000e+00,-1.8394996067926077e+00, 9.1300451472546884e-01)),
                Pair(doubleArrayOf(1.0000000000000000e+00, 0.0000000000000000e+00,-1.0000000000000000e+00),
                     doubleArrayOf(1.0000000000000000e+00,-1.8850158761569491e+00, 9.3077986408630475e-01)),
                Pair(doubleArrayOf(1.0000000000000000e+00,-2.0000000000000000e+00, 1.0000000000000000e+00),
                     doubleArrayOf(1.0000000000000000e+00,-1.8768380595808059e+00, 9.6384222204247760e-01)),
                Pair(doubleArrayOf(1.0000000000000000e+00,-2.0000000000000000e+00, 1.0000000000000000e+00),
                     doubleArrayOf(1.0000000000000000e+00,-1.9345509526653344e+00, 9.7509616589381831e-01)))

    val envelopeDetection: Array<Pair<DoubleArray, DoubleArray>> =
        arrayOf(Pair(doubleArrayOf(4.9757435768686426e-05, 9.9514871537372852e-05, 4.9757435768686426e-05),
                     doubleArrayOf(1.0000000000000000e+00,-9.2730776833100315e-01, 0.0000000000000000e+00)),
                Pair(doubleArrayOf(1.0000000000000000e+00, 1.0000000000000000e+00, 0.0000000000000000e+00),
                     doubleArrayOf(1.0000000000000000e+00,-1.9219313268725426e+00, 9.2740728320254018e-01)))
}}


