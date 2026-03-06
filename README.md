# Joseta

![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/JojoFR1/Joseta/build.yml?logo=githubactions&style=for-the-badge)

## Project

Joseta is a multipurpose, single server, Discord bot made with in Java with the [JDA](https://github.com/discord-jda/JDA) library.

### Why single server?

This bot original purpose is to only be used in the official [Mindustry France](https://discord.com/invite/hzGPWhZSGV) Discord server. It was made without
support for multiple servers nor to be for public usage. This could change in the future.

This bot is not *yet* fully usable for personal use due to multiple hardcoded values that are now mostly removed with
the configuration menu. If you wish to use it you are free to do so, but we do not give any support as of now.

### Why Java and JDA?

We could have used JavaScript, Python or literally any other common languages, but due to personal preferences we chose
Java. The [JDA](https://github.com/discord-jda/JDA) library was a library we already had experience with on top of being popular.

## Features

Being multipurpose and not centered around one specific category or idea. All features implemented, updated or removed
are based of our needs.

Below is non-exhaustive list of the features this bot currently has:

- An **Image-based Welcome** for new members (or message in case the image is disabled or not properly loaded).
- A **Goodbye Message** for leaving members.
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
- Add a Ticket system (probably PR 17 or 18).

- Collect and display statistics (message count, user join/leave)

- Message leaderboard + levels (?).
 
- Add translation for messages & logging (mainly to lower string repetitions and to have it all in one file)

- Support multi-server:
  - Mainly to make it usable outside our server.
  - Make it actually usable for others.

## Building

Building requires **JDK 21** or later.

At the root of the project, use the following command:
`./gradlew shadowJar`

Once the build process is finished, the output will be present in `./build/libs/JosetaBot.jar`.

## Running

You will first need to follow the instructions in the `.env.template` file before being able to run the bot.
**This is mandatory.**

Then, you can execute this command: `java -jar build/libs/JosetaBot.jar` with the following arguments: (after the `.jar`)

- Default: run the bot normally. (Log level is info level)
- `--debug`: run the bot in debug mode. (Log level is debug level & intended for testing)
- `--server`: run the bot in server mode. (Log level is debug level & save log file up to 14 days)

You can also run the `run` gradle task with `./gradlew run`, which is the same as running the jar with the `--debug` argument.

The `runDebug` gradle task is only intended for development purpose with *IntelliJ IDEA*.


## Contributors

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/JojoFR1"><img src="https://avatars.githubusercontent.com/u/110781915?v=4?s=100" width="100px;" alt="Jojo"/><br /><sub><b>Jojo</b></sub></a><br /><a href="https://github.com/JojoFR1/Joseta/commits?author=JojoFR1" title="Code">💻</a> <a href="#ideas-JojoFR1" title="Ideas, Planning, & Feedback">🤔</a> <a href="#maintenance-JojoFR1" title="Maintenance">🚧</a> <a href="#infra-JojoFR1" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://zetamap.fr/"><img src="https://avatars.githubusercontent.com/u/56844734?v=4?s=100" width="100px;" alt="ZetaMap"/><br /><sub><b>ZetaMap</b></sub></a><br /><a href="https://github.com/JojoFR1/Joseta/commits?author=ZetaMap" title="Code">💻</a></td>
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