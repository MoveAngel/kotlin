/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.script

import com.intellij.openapi.project.ProjectManager
import org.jetbrains.kotlin.idea.core.script.ScriptConfigurationManager
import org.jetbrains.kotlin.psi.KtFile

class ScriptConfigurationLoadingTest : AbstractScriptConfigurationLoadingTest() {
    fun testSimple() {
        assertAndLoadInitialConfiguration()

        makeChanges("A")
        assertAndDoAllBackgroundTasks()
        assertSingleLoading()
        assertAndApplySuggestedConfiguration()
        assertAppliedConfiguration("A")
    }

    fun testConcurrentLoadingWhileInQueue() {
        assertAndLoadInitialConfiguration()

        makeChanges("A")
        makeChanges("B")
        assertAndDoAllBackgroundTasks()
        assertSingleLoading()
        assertAndApplySuggestedConfiguration()
        assertAppliedConfiguration("B")
    }

    fun testConcurrentLoadingWhileInQueueABA() {
        assertAndLoadInitialConfiguration()

        makeChanges("A")
        makeChanges("B")
        makeChanges("A")
        assertAndDoAllBackgroundTasks()
        assertSingleLoading()
        assertAndApplySuggestedConfiguration()
        assertAppliedConfiguration("A")
    }

    fun testConcurrentLoadingWhileInQueueABA2() {
        assertAndLoadInitialConfiguration()

        makeChanges("A")
        makeChanges("initial")
        assertAndDoAllBackgroundTasks()
        assertNoLoading()
        assertNoSuggestedConfiguration()
    }

    fun testConcurrentLoadingWhileAnotherLoadInProgress() {
        assertAndLoadInitialConfiguration()

        makeChanges("A")
        assertDoAllBackgroundTaskAndDoWhileLoading {
            makeChanges("B")
        }
        assertSingleLoading()
        assertAndApplySuggestedConfiguration()
        assertAppliedConfiguration("A")

        assertAndDoAllBackgroundTasks()
        assertSingleLoading()
        assertAndApplySuggestedConfiguration()
        assertAppliedConfiguration("B")
    }

    fun testConcurrentLoadingWhileAnotherLoadInProgressABA() {
        assertAndLoadInitialConfiguration()

        makeChanges("A")
        assertDoAllBackgroundTaskAndDoWhileLoading {
            makeChanges("B")
            makeChanges("A")
        }
        assertSingleLoading()
        assertAndApplySuggestedConfiguration()
        assertAppliedConfiguration("A")

        assertAndDoAllBackgroundTasks()
        assertNoLoading()
        assertNoSuggestedConfiguration()
        assertAppliedConfiguration("A")
    }

    fun mutedTestConcurrentLoadingWhileAnotherLoadInProgressABA2() { // todo: fix and unmute
        assertAndLoadInitialConfiguration()

        makeChanges("A")
        assertDoAllBackgroundTaskAndDoWhileLoading {
            makeChanges("initial")
        }
        assertSingleLoading()
        assertSuggestedConfiguration()

        assertAndDoAllBackgroundTasks()
        assertNoLoading()
        assertNoSuggestedConfiguration()
    }

    fun testConcurrentLoadingWhileNotApplied() {
        assertAndLoadInitialConfiguration()

        makeChanges("A")

        assertAndDoAllBackgroundTasks()
        assertSingleLoading()
        assertAppliedConfiguration("initial")

        // we have loaded and not applied configuration for A
        // let's invalidate file again and check that loading will occur

        makeChanges("B")
        assertAndDoAllBackgroundTasks()
        assertSingleLoading()
        assertAndApplySuggestedConfiguration()
        assertAppliedConfiguration("B")
    }

    fun testConcurrentLoadingWhileNotAppliedABA() {
        assertAndLoadInitialConfiguration()

        makeChanges("A")

        assertAndDoAllBackgroundTasks()
        assertSingleLoading()
        assertAppliedConfiguration("initial")

        // we have loaded and not applied configuration for A
        // let's invalidate file and change it back
        // and check that loading will NOT occur

        makeChanges("B")
        makeChanges("A")

        assertAndDoAllBackgroundTasks()
        assertNoLoading()
        assertAppliedConfiguration("initial")

        assertAndApplySuggestedConfiguration()
        assertAppliedConfiguration("A")
    }

    fun testConcurrentLoadingWhileNotAppliedABA2() {
        assertAndLoadInitialConfiguration()

        makeChanges("A")
        assertAndDoAllBackgroundTasks()
        assertSingleLoading()
        assertSuggestedConfiguration()

        makeChanges("initial")
        assertAndDoAllBackgroundTasks()
        assertNoLoading()
        assertNoSuggestedConfiguration()
    }

    fun testLoadingForUsagesSearch() {
        assertAndLoadInitialConfiguration()
        assertTrue(scriptConfigurationManager.updater.ensureConfigurationUpToDate(listOf(myFile as KtFile)))

        changeContents("A")
        assertFalse(scriptConfigurationManager.updater.ensureConfigurationUpToDate(listOf(myFile as KtFile)))
    }

    fun testReportsOnAutoApply() {
        assertAndLoadInitialConfiguration()
        assertReports("initial")
    }

    fun testReportsOnFailedConfiguration() {
        assertAndLoadInitialConfiguration()

        makeChanges("#BAD:A")
        assertAndDoAllBackgroundTasks()
        assertSingleLoading()
        assertNoSuggestedConfiguration()
        assertReports("#BAD:A")
    }

    fun testReportsOnSameConfiguration() {
        assertAndLoadInitialConfiguration()

        makeChanges("initial#IGNORE_IN_CONFIGURATION")
        assertAndDoAllBackgroundTasks()
        assertSingleLoading()
        assertNoSuggestedConfiguration()
        assertAppliedConfiguration("initial")
        assertReports("initial#IGNORE_IN_CONFIGURATION")
    }

    fun testReportsAfterApply() {
        assertAndLoadInitialConfiguration()

        makeChanges("A")
        assertAndDoAllBackgroundTasks()
        assertSingleLoading()
        assertAndApplySuggestedConfiguration()
        assertAppliedConfiguration("A")
        assertReports("A")
    }

    fun testRootsStorage() {
        assertAndLoadInitialConfiguration()

        ProjectManager.getInstance().closeProject(project)
        ProjectManager.getInstance().loadAndOpenProject(project.projectFilePath!!)

        assertAppliedConfiguration("initial")
        assertFalse(ScriptConfigurationManager.hasNewRoots(project))
    }
}