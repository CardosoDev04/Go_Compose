package isel.tds.go.view

import isel.tds.go.model.Position

/**
 * This class is used define how the user input is read.
 */
data class CommandLine(
    val name:String,
    val args: List<String>
)

/**
 * This function is used to read the user input.
 */
fun readCommandLine(): CommandLine {
    print("> ")
    val line = readln().split(" ").filter{ it.isNotBlank() }
    return if (line.isEmpty() || line.size > 2  ) readCommandLine()
        else CommandLine(line.first().uppercase(), line.drop(1))
}

