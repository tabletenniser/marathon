package com.malinskiy.marathon.gradle

import com.malinskiy.marathon.config.FilteringConfiguration
import com.malinskiy.marathon.config.TestFilterConfiguration
import groovy.lang.Closure

open class FilteringPluginConfiguration {
    //groovy
    var groovyAllowList: Wrapper? = null
    var groovyBlockList: Wrapper? = null

    fun allowlist(closure: Closure<*>) {
        groovyAllowList = Wrapper()
        closure.delegate = groovyAllowList
        closure.call()
    }

    fun blocklist(closure: Closure<*>) {
        groovyBlockList = Wrapper()
        closure.delegate = groovyBlockList
        closure.call()
    }

    //kts
    var allowlist: MutableCollection<TestFilterConfiguration> = mutableListOf()
    var blocklist: MutableCollection<TestFilterConfiguration> = mutableListOf()
    fun allowlist(block: MutableCollection<TestFilterConfiguration>.() -> Unit) {
        allowlist.also(block)
    }

    fun blocklist(block: MutableCollection<TestFilterConfiguration>.() -> Unit) {
        blocklist.also(block)
    }
}

open class Wrapper {
    open var simpleClassNameFilter: ArrayList<String>? = null
    open var fullyQualifiedClassnameFilter: ArrayList<String>? = null
    open var fullyQualifiedTestnameFilter: ArrayList<String>? = null
    open var testPackageFilter: ArrayList<String>? = null
    open var testMethodFilter: ArrayList<String>? = null
    open var annotationFilter: ArrayList<String>? = null
    open var annotationDataFilter: ArrayList<String>? = null
    open var allureTestFilter: Boolean = false
}

fun Wrapper.toList(): List<TestFilterConfiguration> {
    val mutableList = mutableListOf<TestFilterConfiguration>()
    when (annotationFilter?.size) {
        null, 0 -> Unit
        1 -> mutableList.add(TestFilterConfiguration.AnnotationFilterConfiguration(annotationFilter?.first().orEmpty().toRegex()))
        else -> mutableList.add(TestFilterConfiguration.AnnotationFilterConfiguration(values = annotationFilter))
    }
    when (fullyQualifiedClassnameFilter?.size) {
        null, 0 -> Unit
        1 -> mutableList.add(TestFilterConfiguration.FullyQualifiedClassnameFilterConfiguration(fullyQualifiedClassnameFilter?.first().orEmpty().toRegex()))
        else -> mutableList.add(TestFilterConfiguration.FullyQualifiedClassnameFilterConfiguration(values = fullyQualifiedClassnameFilter))
    }
    when (testPackageFilter?.size) {
        null, 0 -> Unit
        1 -> mutableList.add(TestFilterConfiguration.TestPackageFilterConfiguration(testPackageFilter?.first().orEmpty().toRegex()))
        else -> mutableList.add(TestFilterConfiguration.TestPackageFilterConfiguration(values = testPackageFilter))
    }
    when (testMethodFilter?.size) {
        null, 0 -> Unit
        1 -> mutableList.add(TestFilterConfiguration.TestMethodFilterConfiguration(testMethodFilter?.first().orEmpty().toRegex()))
        else -> mutableList.add(TestFilterConfiguration.TestMethodFilterConfiguration(values = testMethodFilter))
    }
    when (simpleClassNameFilter?.size) {
        null, 0 -> Unit
        1 -> mutableList.add(TestFilterConfiguration.SimpleClassnameFilterConfiguration(simpleClassNameFilter?.first().orEmpty().toRegex()))
        else -> mutableList.add(TestFilterConfiguration.SimpleClassnameFilterConfiguration(values = simpleClassNameFilter))
    }
    when (fullyQualifiedTestnameFilter?.size) {
        null, 0 -> Unit
        1 -> mutableList.add(TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(fullyQualifiedTestnameFilter?.first().orEmpty().toRegex()))
        else -> mutableList.add(TestFilterConfiguration.FullyQualifiedTestnameFilterConfiguration(values = fullyQualifiedTestnameFilter))
    }
    this.annotationDataFilter?.map {
        val currentData = it.split(",")
        TestFilterConfiguration.AnnotationDataFilterConfiguration(currentData.first().toRegex(), currentData[1].toRegex())
    }?.let {
        mutableList.addAll(it)
    }
    if (allureTestFilter) {
        mutableList.add(TestFilterConfiguration.AllureFilterConfiguration)
    }
    return mutableList
}

fun FilteringPluginConfiguration.toFilteringConfiguration(): FilteringConfiguration {
    if (groovyAllowList != null || groovyBlockList != null) {
        val allow = groovyAllowList?.toList() ?: emptyList()

        val block = groovyBlockList?.toList() ?: emptyList()
        return FilteringConfiguration(allow, block)
    }
    return FilteringConfiguration(allowlist, blocklist)
}

