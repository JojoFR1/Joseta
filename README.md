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
- A **Counting Channel** to let users count until infinity.
- Multiple commands:

  | Category   | Commands       | Use                                                                                    |
  |:-----------|:---------------|:---------------------------------------------------------------------------------------|
  |            | `/config`      | Configure the bot variables for your server. Use a GUI like comoponent message.        |
  | Misc.      | `/ping`        | Get the bot and Discord API ping in milliseconds.                                      |
  |            | `/multi`       | Manually send the multiplayer help message.                                            |
  |            | `/markov`      | (Not implemented!) Generate a pseudo-random message using messages sent on the server. |
  |            | `/reminder`    | Add or (list not implemented) reminders.                                               |
  | Moderation | `/modlog`      | Get a member current moderation history.                                               |
  |            | `/(un)ban`     | (Un)Ban a member from the server.                                                      |
  |            | `/kick`        | Kick a member from the server.                                                         |
  |            | `/(un)timeout` | (Un)Timeout a member on the server.                                                    |
  |            | `/(un)warn`    | (Un)Warn a member on the server.                                                       |
  |            | `/clear`       | Clear messages in the channel.                                                         |

## Plans

The current plans and ideas for this bot are the following, in the likely order in which they will be made:

- Add a Logging system ([PR #15](https://github.com/JojoFR1/Joseta/pull/15)):
- Add a Ticket system

- Message leaderboard + levels (?).
 
- Add translation for messages & logging (mainly to lower string repetitions and to have it all in one file)

- Support multi-server:
  - Mainly to make it usable outside our server.
  - Make it actually usable for others.

## How to build

Building requires **JDK 21** or later.

At the root of the project, use the following command:
`./gradlew shadowJar`

Once the build process is finished, the output will be present in `./build/libs/JosetaBot.jar`.

## How to run

You will first need to follow the instructions in the `.env.template` file before being able to run the bot.

After this, starting the bot is very easy. You need to execute this command: `java -jar build/libs/JosetaBot.jar`

The following arguments (after the `.jar`) are available to use:

- Default: run the bot normally. (Log level is info level)
- `--debug`: run the bot in debug mode. (Log level is debug level & intended for testing)
- `--server`: run the bot in server mode. (Log level is debug level & save log file up to 14 days)

## Contributors

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/JojoFR1"><img src="https://avatars.githubusercontent.com/u/110781915?v=4?s=100" width="100px;" alt="Jojo"/><br /><sub><b>Jojo</b></sub></a><br /><a href="https://github.com/JojoFR1/Joseta/commits?author=JojoFR1" title="Code">💻</a> <a href="#ideas-JojoFR1" title="Ideas, Planning, & Feedback">🤔</a> <a href="#maintenance-JojoFR1" title="Maintenance">🚧</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://zetamap.fr/"><img src="https://avatars.githubusercontent.com/u/56844734?v=4?s=100" width="100px;" alt="ZetaMap"/><br /><sub><b>ZetaMap</b></sub></a><br /><a href="https://github.com/JojoFR1/Joseta/commits?author=ZetaMap" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://xorblo-doitus.github.io/projects/"><img src="https://avatars.githubusercontent.com/u/75997617?v=4?s=100" width="100px;" alt="Patou"/><br /><sub><b>Patou</b></sub></a><br /><a href="https://github.com/JojoFR1/Joseta/commits?author=xorblo-doitus" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://l4p1n.ch/"><img src="https://avatars.githubusercontent.com/u/3647174?v=4?s=100" width="100px;" alt="l4p1n (Mathias B.)"/><br /><sub><b>l4p1n (Mathias B.)</b></sub></a><br /><a href="https://github.com/JojoFR1/Joseta/commits?author=lapin-b" title="Code">💻</a> <a href="#ideas-lapin-b" title="Ideas, Planning, & Feedback">🤔</a> <a href="#mentoring-lapin-b" title="Mentoring">🧑‍🏫</a> <a href="#infra-lapin-b" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a></td>
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