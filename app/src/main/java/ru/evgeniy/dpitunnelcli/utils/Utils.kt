package ru.evgeniy.dpitunnelcli.utils

object Utils {
    fun getOpt(args: List<String>) = args.fold(Pair(emptyMap<String, List<String>>(), "")) { (map, lastKey), elem ->
        if (elem.startsWith("-"))  Pair(map + (elem to emptyList()), elem)
        else Pair(map + (lastKey to map.getOrElse(lastKey) { emptyList() } + elem), lastKey)
    }.first
}