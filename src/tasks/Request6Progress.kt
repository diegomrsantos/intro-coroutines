package tasks

import contributors.*

suspend fun loadContributorsProgress(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    val repos = service
        .getOrgRepos(req.org)
        .also { logRepos(req, it) }
        .bodyList()

    val users = mutableListOf<User>()
    for ((index, repo) in repos.withIndex()) {
        users += service.getRepoContributors(req.org, repo.name)
            .also { logUsers(repo, it) }
            .bodyList()
        updateResults(users.aggregate(), index == repos.lastIndex)
    }
}
