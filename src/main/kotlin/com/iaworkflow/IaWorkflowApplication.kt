package com.iaworkflow

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication
class IaWorkflowApplication

fun main(args: Array<String>) {
    runApplication<IaWorkflowApplication>(*args)
}
