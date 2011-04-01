/**
 * Mule Git Connector
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.git;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link GitConnector}
 * 
 * @author Mariano A Cortesi
 * @since Apr 1, 2011
 */
public class GitConnectorTest {

    private static final String REMOTE_REPO = "src/test/resources/base-git-repo";
    private final String testRepoLocation = "src/test/resources/cloned-repository";
    private File location = new File(testRepoLocation);
    private GitConnector gitConnector;

    @Before
    public void prepareExternalRepo() throws IOException {
        FileUtils.moveDirectory(new File(REMOTE_REPO, "git"), new File(REMOTE_REPO, ".git"));
    }
    
    @Before
    public void checkNoRepos() {
        assertFalse(location.exists());
    }
    
    @Before
    public void createConnector() {
        gitConnector = new GitConnector();
        gitConnector.setDirectory(testRepoLocation);
    }
    
    @Test
    public void testClone() {
        gitConnector.cloneRepository(REMOTE_REPO, false, "origin", "HEAD", testRepoLocation);
        assertTrue(location.exists());
        assertTrue(new File(location, "a").exists());
    }
    
    @Test
    public void testAddAndCommit() throws IOException {
        gitConnector.cloneRepository(REMOTE_REPO, false, "origin", "HEAD", null);
        
        FileUtils.touch(new File(location, "new-file"));
        gitConnector.add("new-file", null);
        
        gitConnector.commit("new file added", "test guy", "test@mail.com", "test guy", "test@mail.com", null);
    }

    @Test
    public void testCreateBranchFromRemoteRepo() {
        gitConnector.cloneRepository(REMOTE_REPO, false, "origin", "HEAD", null);
        gitConnector.createBranch("my-branch", false, "origin/test-branch", null);
        assertTrue(new File(location, "b").exists()); 
    }
    
    @Test
    public void testCheckoutBranchFromRemoteRepo() {
        gitConnector.cloneRepository(REMOTE_REPO, false, "origin", "HEAD", null);
        gitConnector.checkout("my-branch", "origin/test-branch", null);
        assertTrue(new File(location, "b").exists()); 
    }
    
    @Test
    public void testCheckoutLocalBranch() {
        gitConnector.cloneRepository(REMOTE_REPO, false, "origin", "HEAD", null);
        gitConnector.createBranch("my-branch", false, "origin/test-branch", null);
        gitConnector.checkout("my-branch", null, null);
        assertTrue(new File(location, "b").exists()); 
    }
    
    @Test
    public void testFetch() {
        gitConnector.cloneRepository(REMOTE_REPO, false, "origin", "HEAD", null);
        gitConnector.fetch(null);
    }
    
    
    @After
    public void cleanUpDirectories() throws IOException {
        FileUtils.deleteDirectory(location);
    }
    
    @After
    public void releaseExternalRepo() throws IOException {
        FileUtils.moveDirectory(new File(REMOTE_REPO, ".git"), new File(REMOTE_REPO, "git"));
    }
}
