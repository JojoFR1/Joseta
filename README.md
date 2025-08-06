# Joseta

![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/JojoFR1/Joseta/build.yml?logo=githubactions&style=for-the-badge)

## Project

Joseta is a multipurpose, single server, Discord bot made with the [JDA](https://github.com/discord-jda/JDA) library.

### Why single server?

This bot original purpose is to only be used in the official [Mindustry France](https://discord.com/invite/hzGPWhZSGV) Discord server only - so it was made without any plans to support multiple servers nor to be a public bot for now, this could change in the future. Features are only implemented based of our needs.

This bot is not *yet* fully usable for personal use due to multiple hardcoded values that are mostly now removed with the configuration command. If you wish to use it you are free to do so buy we do not give any support as of now.

### Why Java and JDA?

We could have used JavaScript or Python or literally any other languages, but due to personal preferences we have chosen Java and the JDA library as it was a library we already had experience with. Discussion for a possible Kotlin transition will be explored.

## Features

This is a multipurpose bot and is not centered around one specific category or idea, all features implemented (updated or removed) are based only on our needs. See below for a list of all the features this bot currently has:

- An **Image-based Welcome** for new members (or message in case the image is disabled or not properly loaded).
- A **Goodbye Message** for leaving members.
- A **Counting Channel** to count until infinity.
- **Rules** + an **Acceptation System**, acting as verification too (not *yet* configurable).
- An **Auto Response** for common questions (not *yet* configurable but can be disabled).
- Multiple commands:

  | Category   | Commands       | Use                                                                 |
  |:-----------|:---------------|:--------------------------------------------------------------------|
  | Admin      | `/admin`       | Only used to send and update rules.                                 |
  |            | `/config`      | Configure the bot variables for your server.                        |
  | Misc.      | `/ping`        | Get the bot and Discord API ping in milliseconds.                   |
  |            | `/multi`       | Manually send the multiplayer help message.                         |
  |            | `/markov`      | Generate a pseudo-random message using messages sent on the server. |
  | Moderation | `/modlog`      | Get a member current moderation history.                            |
  |            | `/(un)ban`     | (Un)Ban a member from the server.                                   |
  |            | `/kick`        | Kick a member from the server.                                      |
  |            | `/(un)timeout` | (Un)Timeout a member on the server.                                 |
  |            | `/(un)warn`    | (Un)Warn a member on the server.                                    |
  |            | `/clear`       | Clear messages in the channel.                                      |

## Plans

The current plans and ideas for this bot are the following, in the likely order in which they will be made:

- Add a reminder command

- Refactor the Event system
  - Could do like one single global `ListenerAdapter` and add in the appropriate event the function we want to execute
  - Maybe use annotations system (could do the same for commands)
  - Other implementations could be explored (but I do not have any idea for now)

- Rework config command to use embed instead of multiple arguments command

- Add translation for messages & logging (mainly to lower string repetitions and to have it all in one file)

- Add a Logging system (currently on pause, see [PR #6](https://github.com/JojoFR1/Joseta/pull/6)):
  - Log the warns of the bot.

- Message leaderboard + levels (?).

- Support multi-server:
  - Mainly to make it usable outside our server.
  - Make it actually usable for others.

- Possible transition to Kotlin.

## How to build

Building requires **JDK 17** or later.

At the root of the project, use the following command:
`./gradlew shadowJar`

Once the build process is finished, the output will be present in `./build/libs/JosetaBot.jar`.

## How to run

You will first need to follow the instructions in the `secret_template.cfg` file before being able to run the bot.

After this, starting the bot is very easy. You need to execute this command: `java -jar build/libs/JosetaBot.jar`

The following arguments (after the `.jar`) are available to use:

- Default: run the bot normally. (Log level is info level)
- `--debug`: run the bot in debug mode. (Log level is debug level & intended for testing)
- `--server`: run the bot in server mode. (Log level is debug level & save log file up to 7 days)

## Contributors

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/JojoFR1"><img src="https://avatars.githubusercontent.com/u/110781915?v=4?s=100" width="100px;" alt="Jojo"/><br /><sub><b>Jojo</b></sub></a><br /><a href="https://github.com/JojoFR1/Joseta/commits?author=JojoFR1" title="Code">💻</a> <a href="#ideas-JojoFR1" title="Ideas, Planning, & Feedback">🤔</a> <a href="#maintenance-JojoFR1" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://zetamap.fr/"><img src="https://avatars.githubusercontent.com/u/56844734?v=4?s=100" width="100px;" alt="ZetaMap"/><br /><sub><b>ZetaMap</b></sub></a><br /><a href="#infra-ZetaMap" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://xorblo-doitus.github.io/projects/"><img src="https://avatars.githubusercontent.com/u/75997617?v=4?s=100" width="100px;" alt="Patou"/><br /><sub><b>Patou</b></sub></a><br /><a href="https://github.com/JojoFR1/Joseta/commits?author=xorblo-doitus" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://l4p1n.ch/"><img src="https://avatars.githubusercontent.com/u/3647174?v=4?s=100" width="100px;" alt="l4p1n (Mathias B.)"/><br /><sub><b>l4p1n (Mathias B.)</b></sub></a><br /><a href="https://github.com/JojoFR1/Joseta/commits?author=lapin-b" title="Code">💻</a> <a href="#ideas-lapin-b" title="Ideas, Planning, & Feedback">🤔</a> <a href="#mentoring-lapin-b" title="Mentoring">🧑‍🏫</a></td>
    </tr>
  </tbody>
  <tfoot>
    <tr>
      <td align="center" size="13px" colspan="7">
        <img src="https://raw.githubusercontent.com/all-contributors/all-contributors-cli/1b8533af435da9854653492b1327a23a4dbd0a10/assets/logo-small.svg">
          <a href="https://all-contributors.js.org/docs/en/bot/usage">Add your contributions</a>
        </img>
      </td>
    </tr>
  </tfoot>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!