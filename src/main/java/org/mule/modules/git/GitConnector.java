package org.mule.modules.git;

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
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.modules.git.config.ConnectorConfig;

@Connector(name = "git", friendlyName = "Git")
public class GitConnector {

	@Config
	ConnectorConfig config;

	/**
	 * Clone a repository into a new directory or resset it if it exists
	 *
	 * {@sample.xml ../../../doc/mule-module-git.xml.sample git:clone}
	 *
	 * @param uri
	 *            The (possibly remote) repository to clone from.
	 * @param bare
	 *            True if you want a bare Git repository, false otherwise.
	 * @param remote
	 *            Name of the remote to keep track of the upstream repository.
	 * @param branch
	 *            Name of the local branch into which the remote will be cloned.
	 * @param overrideDirectory
	 *            Name of the directory to use for git repository
	 */
	@Processor
	public void cloneRepository(String uri, @Default("false") boolean bare, @Default("origin") String remote, @Default("HEAD") String branch,
			@Optional String overrideDirectory) {
		File dir = resolveDirectory(overrideDirectory);
		boolean dirExists = dir.exists();

		if (!dirExists) {
			if (!dir.mkdirs()) {
				throw new RuntimeException("Directory " + dir.getAbsolutePath() + " cannot be created");
			}
		}
		Git git = null;
		try {
			if (dirExists) {
				git = Git.open(dir);
				git.log().setMaxCount(1).call();
			} else {
				CloneCommand cloneCommand = Git.cloneRepository();
				cloneCommand.setCredentialsProvider(config.getCredentialsProvider());
				cloneCommand.setBare(bare);
				cloneCommand.setDirectory(dir);
				cloneCommand.setRemote(remote);
				cloneCommand.setBranch(branch);
				cloneCommand.setURI(uri);
				git = cloneCommand.call();
			}
		} catch (Exception e) {
			throw new RuntimeException("Cannot clone or open repository", e);
		} finally {
			if (git != null) {
				git.close();
			}
		}

	}

	@Processor
	public void resetRepository(@Default("HEAD") String branch, @Optional String overrideDirectory) {
		Git git = getGitIntance(overrideDirectory);
		try {
			git.reset().setMode(ResetType.HARD).setRef(branch).call();
		} catch (Exception e) {
			throw new RuntimeException("Cannot reset repository", e);
		} finally {
			if (git != null) {
				git.close();
			}
		}
	}

	/**
	 * Add file contents to the index
	 *
	 * {@sample.xml ../../../doc/mule-module-git.xml.sample git:add}
	 *
	 * @param filePatterns
	 *            List of file patterns to add content from. Also a leading directory name (e.g. dir to add dir/file1 and dir/file2) can be given to add all
	 *            files in the directory, recursively. Use semicolon (";") to separete patterns
	 * @param forceAll
	 *            Add all files
	 * @param overrideDirectory
	 *            Name of the directory to use for git repository
	 */
	@Processor
	public void add(@Optional String filePatterns, @Default("false") boolean forceAll, @Optional String overrideDirectory) {
		Git git = getGitIntance(overrideDirectory);
		try {
			AddCommand add = git.add();
			if (filePatterns != null) {
				for (String file : filePatterns.split(";")) {
					if (!file.trim().isEmpty()) {
						add.addFilepattern(file.trim());
					}
				}
			}
			if (forceAll) {
				add.addFilepattern(".");
			}
			add.call();
		} catch (Exception e) {
			throw new RuntimeException("Cannot add " + filePatterns, e);
		} finally {
			if (git != null) {
				git.close();
			}
		}
	}

	/**
	 * Create a local branch
	 *
	 * {@sample.xml ../../../doc/mule-module-git.xml.sample git:create-branch}
	 *
	 * @param branchName
	 *            Name of the new branch
	 * @param force
	 *            If true and the branch with the given name already exists, the start-point of an existing branch will be set to a new start-point; if false,
	 *            the existing branch will not be changed.
	 * @param startPoint
	 *            The new branch head will point to this commit. It may be given as a branch name, a commit-id, or a tag. If this option is omitted, the current
	 *            HEAD will be used instead.
	 * @param overrideDirectory
	 *            Name of the directory to use for git repository
	 */
	@Processor
	public void createBranch(String branchName, @Default("false") boolean force, @Default("HEAD") String startPoint, @Optional String overrideDirectory) {
		Git git = getGitIntance(overrideDirectory);
		try {
			CreateBranchCommand createBranch = git.branchCreate();
			createBranch.setName(branchName);
			createBranch.setForce(force);
			createBranch.setStartPoint(startPoint);
			createBranch.call();
			git.close();
		} catch (Exception e) {
			throw new RuntimeException("Unable to create branch " + branchName, e);
		} finally {
			if (git != null) {
				git.close();
			}
		}
	}

	/**
	 * Delete local branch
	 *
	 * {@sample.xml ../../../doc/mule-module-git.xml.sample git:delete-branch}
	 *
	 * @param branchName
	 *            Name of the branch to delete
	 * @param force
	 *            If false a check will be performed whether the branch to be deleted is already merged into the current branch and deletion will be refused in
	 *            this case
	 * @param overrideDirectory
	 *            Name of the directory to use for git repository
	 */
	@Processor
	public void deleteBranch(String branchName, boolean force, @Optional String overrideDirectory) {
		Git git = getGitIntance(overrideDirectory);
		try {
			DeleteBranchCommand deleteBranch = git.branchDelete();
			deleteBranch.setBranchNames(branchName);
			deleteBranch.setForce(force);

			deleteBranch.call();
		} catch (Exception e) {
			throw new RuntimeException("Unable to create branch " + branchName, e);
		} finally {
			if (git != null) {
				git.close();
			}
		}
	}

	/**
	 * Record changes to the repository
	 *
	 * {@sample.xml ../../../doc/mule-module-git.xml.sample git:commit}
	 *
	 * @param msg
	 *            Commit message
	 * @param committerName
	 *            Name of the person performing this commit
	 * @param committerEmail
	 *            Email of the person performing this commit
	 * @param authorName
	 *            Name of the author of the changes to commit
	 * @param authorEmail
	 *            Email of the author of the changes to commit
	 * @param all
	 *            If set to true the Commit command automatically stages files that have been modified and deleted, but new files not known by the repository
	 *            are not affected.
	 * @param overrideDirectory
	 *            Name of the directory to use for git repository
	 */
	@Processor
	public void commit(String msg, String committerName, String committerEmail, @Optional String authorName, @Optional String authorEmail,
			@Default("false") boolean all, @Optional String overrideDirectory) {
		Git git = getGitIntance(overrideDirectory);

		try {
			CommitCommand commit = git.commit();
			if (authorName != null && authorEmail != null) {
				commit.setAuthor(authorName, authorEmail);
			}

			commit.setCommitter(committerName, committerEmail);
			commit.setMessage(msg);
			commit.setAll(all);

			commit.call();
		} catch (Exception e) {
			throw new RuntimeException("Unable to commit", e);
		} finally {
			if (git != null) {
				git.close();
			}
		}
	}

	/**
	 * Update remote refs along with associated objects
	 *
	 * {@sample.xml ../../../doc/mule-module-git.xml.sample git:push}
	 *
	 * @param remote
	 *            The remote (uri or name) used for the push operation.
	 * @param force
	 *            Sets the force preference for push operation
	 * @param overrideDirectory
	 *            Name of the directory to use for git repository
	 */
	@Processor
	public void push(@Default("origin") String remote, @Default("false") boolean force, @Optional String overrideDirectory) {
		Git git = getGitIntance(overrideDirectory);

		try {
			PushCommand push = git.push();
			push.setCredentialsProvider(config.getCredentialsProvider());
			push.setRemote(remote);
			push.setForce(force);

			push.call();
		} catch (Exception e) {
			throw new RuntimeException("Unable to push to " + remote, e);
		} finally {
			if (git != null) {
				git.close();
			}
		}

	}

	/**
	 * Fetch from and merge with another repository or a local branch
	 *
	 * {@sample.xml ../../../doc/mule-module-git.xml.sample git:pull}
	 *
	 * @param overrideDirectory
	 *            Name of the directory to use for git repository
	 */
	@Processor
	public void pull(@Optional String overrideDirectory) {
		Git git = getGitIntance(overrideDirectory);

		try {
			PullCommand pull = git.pull();
			pull.setCredentialsProvider(config.getCredentialsProvider());

			pull.call();
		} catch (Exception e) {
			throw new RuntimeException("Unable to pull", e);
		} finally {
			if (git != null) {
				git.close();
			}
		}
	}

	/**
	 * Fetch changes from another repository
	 *
	 * {@sample.xml ../../../doc/mule-module-git.xml.sample git:fetch}
	 *
	 * @param overrideDirectory
	 *            Name of the directory to use for git repository
	 */
	@Processor
	public void fetch(@Optional String overrideDirectory) {
		Git git = getGitIntance(overrideDirectory);

		try {
			FetchCommand fetch = git.fetch();
			fetch.setCredentialsProvider(config.getCredentialsProvider());

			fetch.call();
		} catch (Exception e) {
			throw new RuntimeException("Unable to fetch", e);
		} finally {
			if (git != null) {
				git.close();
			}
		}
	}

	/**
	 * Checkout a local branch or create a local branch from a remote branch
	 *
	 * {@sample.xml ../../../doc/mule-module-git.xml.sample git:checkout}
	 *
	 * or
	 *
	 * {@sample.xml ../../../doc/mule-module-git.xml.sample git:checkout}
	 *
	 * @param startPoint
	 *            If specified creates a new branch pointing to this startPoint
	 * @param branch
	 *            Name of the branch to checkout
	 * @param overrideDirectory
	 *            Name of the directory to use for git repository
	 */
	@Processor
	public void checkout(String branch, @Optional String startPoint, @Optional String overrideDirectory) {
		Git git = getGitIntance(overrideDirectory);

		try {
			CheckoutCommand checkout = git.checkout();

			checkout.setName(branch);
			if (startPoint != null) {
				checkout.setCreateBranch(true);
				checkout.setStartPoint(startPoint);
			}

			checkout.call();
		} catch (Exception e) {
			throw new RuntimeException("Unable to fetch", e);
		} finally {
			if (git != null) {
				git.close();
			}
		}
	}

	/**
	 * Depending on the value of overrideDirectory, it returns the directory to use as the git repo.
	 * 
	 * @param overrideDirectory
	 *            path or null if default is required
	 * @return file pointing to repository directory to use
	 */
	private File resolveDirectory(String overrideDirectory) {
		File dir;
		if (overrideDirectory == null) {
			dir = new File(config.getDirectory());
		} else {
			dir = new File(overrideDirectory);
		}
		return dir;
	}

	// private Repository getGitRepo(String overrideDirectory) throws IOException {
	// File dir = resolveDirectory(overrideDirectory);
	// if (!dir.exists()) {
	// throw new RuntimeException("Directory " + dir.getAbsolutePath() + " does not exists");
	// }
	//
	// FileRepositoryBuilder builder = new FileRepositoryBuilder();
	// Repository repository = builder.setGitDir(dir).readEnvironment().findGitDir().build();
	// return repository;
	// }

	private Git getGitIntance(String overrideDirectory) {
		File dir = resolveDirectory(overrideDirectory);
		Git git = null;
		try {
			git = Git.open(dir);
		} catch (IOException e) {
			throw new RuntimeException("Could not open Git repository", e);
		}
		return git;
	}

	public ConnectorConfig getConfig() {
		return config;
	}

	public void setConfig(ConnectorConfig config) {
		this.config = config;
	}

}