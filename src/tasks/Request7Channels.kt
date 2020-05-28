package tasks

import contributors.*
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun loadContributorsChannels(
    service: GitHubService,
    req: RequestData,
    updateResults: suspend (List<User>, completed: Boolean) -> Unit
) {
    coroutineScope {
        val channel = Channel<List<User>>()
        val repos = service
            .getOrgRepos(req.org)
            .also { logRepos(req, it) }
            .bodyList()
        for (repo in repos) {
            launch {
                val repoContributors = service.getRepoContributors(req.org, repo.name)
                    .also { logUsers(repo, it) }
                    .bodyList()
                channel.send(repoContributors)
            }
        }
        val allUsers = mutableListOf<User>()
        repeat(repos.size) {
            allUsers += channel.receive()
            updateResults(allUsers.aggregate(), it == repos.lastIndex)
        }
    }
}
