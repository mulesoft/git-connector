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

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.mule.tools.cloudconnect.annotations.Connector;
import org.mule.tools.cloudconnect.annotations.Operation;
import org.mule.tools.cloudconnect.annotations.Parameter;
import org.mule.tools.cloudconnect.annotations.Property;

@Connector(namespacePrefix = "git")
public class GitConnector
{

    /**
     * Directory of your git repository
     */
    @Property
    private String directory;


    /**
     * Clone a repository into a new directory
     *
     * {@code
     * <git:clone config-ref="s3repo" uri="git@github.com:mulesoft/s3-connector.git"/>
     * }
     *
     * @param uri The (possibly remote) repository to clone from.
     * @param bare True if you want a bare Git repository, false otherwise.
     * @param remote Name of the remote to keep track of the upstream repository.
     * @param branch Name of the local branch into which the remote will be cloned.
     * @param overrideDirectory Name of the directory to use for git repository
     */
    @Operation(name = "clone")
    public void cloneRepository(String uri, @Parameter(optional = true, defaultValue = "false") boolean bare, @Parameter(optional = true, defaultValue = "origin") String remote,
                                @Parameter(optional = true, defaultValue = "HEAD") String branch, @Parameter(optional = true) String overrideDirectory)
    {
        File dir = resolveDirectory(overrideDirectory);
        
        if (!dir.exists())
        {
            if (!dir.mkdirs())
            {
                throw new RuntimeException("Directory " + dir.getAbsolutePath() + " cannot be created");
            }
        }

        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setBare(bare);
        cloneCommand.setDirectory(dir);
        cloneCommand.setRemote(remote);
        cloneCommand.setBranch(branch);
        cloneCommand.setURI(uri);

        try
        {
            Git git = cloneCommand.call();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Cannot clone repository", e);
        }
    }

    /**
     * Add file contents to the index
     *
     * {@code
     * <git:add config-ref="s3repo" filePattern="README.txt"/>
     * }
     *
     * @param filePattern File to add content from. Also a leading directory name (e.g. dir to add dir/file1 and dir/file2) can be given to add all files in the directory, recursively.
     * @param overrideDirectory Name of the directory to use for git repository
     */
    @Operation
    public void add(String filePattern, @Parameter(optional = true) String overrideDirectory)
    {
        try
        {
            Git git = new Git(getGitRepo(overrideDirectory));
            AddCommand add = git.add();
            add.addFilepattern(filePattern);

            add.call();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Cannot add " + filePattern, e);
        }
    }

    /**
     * Create a local branch
     *
     * {@code
     * <git:create-branch config-ref="s3repo" name="myexperiment"
     * startPoint="bd1c1156a06576f4339af4cb9a5cfddfcc80154e">
     * }
     *
     * @param name       Name of the new branch
     * @param force      If true and the branch with the given name already exists, the start-point of an existing branch will be set to a new start-point; if false, the existing branch will not be changed.
     * @param startPoint The new branch head will point to this commit. It may be given as a branch name, a commit-id, or a tag. If this option is omitted, the current HEAD will be used instead.
     * @param overrideDirectory Name of the directory to use for git repository
     */
    @Operation
    public void createBranch(String name, @Parameter(optional = true, defaultValue = "false") boolean force, @Parameter(optional = true, defaultValue = "HEAD") String startPoint, @Parameter(optional = true) String overrideDirectory)
    {
        try
        {
            Git git = new Git(getGitRepo(overrideDirectory));
            CreateBranchCommand createBranch = git.branchCreate();
            createBranch.setName(name);
            createBranch.setForce(force);
            createBranch.setStartPoint(startPoint);
            createBranch.call();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to create branch " + name, e);
        }
    }

    /**
     * Delete local branch
     *
     * {@code
     * <git:delete-branch config-ref="s3repo" name="myexperiment"/>
     * }
     *
     * @param name  Name of the branch to delete
     * @param force If false a check will be performed whether the branch to be deleted is already merged into the current branch and deletion will be refused in this case
     * @param overrideDirectory Name of the directory to use for git repository
     */
    @Operation
    public void deleteBranch(String name, boolean force, @Parameter(optional = true) String overrideDirectory)
    {
        try
        {
            Git git = new Git(getGitRepo(overrideDirectory));
            DeleteBranchCommand deleteBranch = git.branchDelete();
            deleteBranch.setBranchNames(name);
            deleteBranch.setForce(force);

            deleteBranch.call();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to create branch " + name, e);
        }
    }

    /**
     * Record changes to the repository
     *
     * {@code
     * <git:commit config-ref="s3repo" msg="Updated README.txt" committerName="John Doe"
     *             committerEmail="john@doe.net">
     * }
     *
     * @param msg            Commit message
     * @param committerName  Name of the person performing this commit
     * @param committerEmail Email of the person performing this commit
     * @param authorName     Name of the author of the changes to commit
     * @param authorEmail    Email of the author of the changes to commit
     * @param overrideDirectory Name of the directory to use for git repository
     */
    @Operation
    public void commit(String msg, String committerName, String committerEmail, @Parameter(optional = true) String authorName, @Parameter(optional = true) String authorEmail, @Parameter(optional = true) String overrideDirectory)
    {
        try
        {
            Git git = new Git(getGitRepo(overrideDirectory));
            CommitCommand commit = git.commit();
            if (authorName != null && authorEmail != null)
            {
                commit.setAuthor(authorName, authorEmail);
            }

            commit.setCommitter(committerName, committerEmail);
            commit.setMessage(msg);

            commit.call();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to commit"
                    , e);
        }
    }

    /**
     * Update remote refs along with associated objects
     *
     * {@code
     * <git:push config-ref="s3repo" remote="origin"/>
     * }
     *
     * @param remote The remote (uri or name) used for the push operation.
     * @param force  Sets the force preference for push operation
     * @param overrideDirectory Name of the directory to use for git repository
     */
    @Operation
    public void push(@Parameter(optional = true, defaultValue = "origin") String remote, @Parameter(optional = true, defaultValue = "false") boolean force, @Parameter(optional = true) String overrideDirectory)
    {
        try
        {
            Git git = new Git(getGitRepo(overrideDirectory));
            PushCommand push = git.push();
            push.setRemote(remote);
            push.setForce(force);

            push.call();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to push to " + remote, e);
        }

    }

    /**
     * Fetch from and merge with another repository or a local branch
     *
     * {@code
     * <git:pull config-ref="s3repo"/>
     * }
     *
     * @param overrideDirectory Name of the directory to use for git repository
     */
    @Operation
    public void pull(@Parameter(optional = true) String overrideDirectory)
    {
        try
        {
            Git git = new Git(getGitRepo(overrideDirectory));
            PullCommand pull = git.pull();
            pull.call();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to pull", e);
        }

    }
    
    /**
     * Fetch changes from another repository 
     *
     * {@code
     * <git:fetch config-ref="s3repo"/>
     * }
     *
     * @param overrideDirectory Name of the directory to use for git repository
     */
    @Operation
    public void fetch(@Parameter(optional = true) String overrideDirectory)
    {
        try
        {
            Git git = new Git(getGitRepo(overrideDirectory));
            FetchCommand fetch = git.fetch();
            fetch.call();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to fetch", e);
        }
        
    }
    
    /**
     * Checkout a local branch or create a local branch from a remote branch
     *
     * {@code
     * <git:checkout config-ref="s3repo" branch="my-topic-branch" startPoint="origin/my-topic-branch"/>
     * }
     *
     * or 
     *
     * {@code
     * <git:checkout config-ref="s3repo" branch="my-topic-branch"/>
     * }
     *
     * @param startPoint If specified creates a new branch pointing to this startPoint
     * @param branch Name of the branch to checkout
     * @param overrideDirectory Name of the directory to use for git repository
     */
    @Operation
    public void checkout(String branch, @Parameter(optional = true) String startPoint, @Parameter(optional = true) String overrideDirectory)
    {
        try
        {
            Git git = new Git(getGitRepo(overrideDirectory));
            CheckoutCommand checkout = git.checkout();
            
            checkout.setName(branch);
            if (startPoint != null) {
                checkout.setCreateBranch(true);
                checkout.setStartPoint(startPoint);
            }
            
            checkout.call();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to fetch", e);
        }
        
    }

    /**
     * Depending on the value of overrideDirectory, it returns the directory to
     * use as the git repo.
     * 
     * @param overrideDirectory
     *            path or null if default is required
     * @return file pointing to repository directory to use
     */
    private File resolveDirectory(String overrideDirectory) {
        File dir;
        if (overrideDirectory == null) {
            dir = new File(this.directory);
        } else {
            dir = new File(overrideDirectory);
        }
        return dir;
    }

    private Repository getGitRepo(String overrideDirectory) throws IOException
    {
        File dir = resolveDirectory(overrideDirectory);
        if (!dir.exists())
        {
            throw new RuntimeException("Directory " + dir.getAbsolutePath() + " does not exists");
        }

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.setWorkTree(dir);
//        builder.setGitDir(dir);
//        builder.readEnvironment();
//        builder.findGitDir();
        return builder.build();
    }

    public String getDirectory()
    {
        return directory;
    }

    public void setDirectory(String directory)
    {
        this.directory = directory;
    }
}
