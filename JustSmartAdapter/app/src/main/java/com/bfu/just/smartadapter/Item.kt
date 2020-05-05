package com.bfu.just.smartadapter

interface Item

data class Title(var title: String) : Item

data class Person(var name: String, var age: Int) : Item

