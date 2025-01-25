# Joseta

![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/JojoFR1/Joseta/build.yml?logo=githubactions&style=for-the-badge)

## Project

Joseta is a multi purpose, single server, Discord bot made with the [JDA](https://github.com/discord-jda/JDA) library.

### Why single server ?

This bot original purpose is to be used in the official [Mindustry France](https://discord.com/invite/hzGPWhZSGV) Discord server only, and so it was made without any plans to support multiple servers nor to be a public bot. Features are also only made based of our needs as of now.

This bot is not *yet* fully usable for personnal use due to multiple hardcoded values, but it is in our plan to make it usable outside of our server. If you wish to use it, we won't give any support for now.

### Why Java and JDA?

We had the choices between JavaScript, Python or Java. Due to personnal preferences, we chose Java and JDA was a library we already had experience in.

## Features

The bot currently has those features available:

- Multiple commands:
  - A `/admin`, only used to send rules as of now.
  - For moderation:
      - A `/(un)warn` command to (un)warn a member.
      - A `/(un)mute` command to (un)mute a member.
      - A `/kick` command to kick a member.
      - A `/(un)ban` command to (un)ban a member.
      - A `/modlog` command to acces a member moderation history.
- Multiple features:
  - An **Image based Welcome** for new members (not *yet* configurable)
  - A **Leave Message** for quitting members (not *yet* configurable)
  - **Rules** + an **Acceptation System**, acting as a verification too (not *yet* configurable)
  - An **Auto Response** for a common question (not *yet* configurable)

## Plans

Our current plans for the future are the following:

- Moderation commands:
  - Support User IDs for `member` argument.
  - Make 'un-' commands actually work (remove sanction)
  - Make it able to use a user id
  - Better message response (sucess & failed)

- Add a `/config` command
  - Allow to change *some* variable without having to rebuild and restart
  - Allow to un-hardcode values
  - Allow to be Runnable outside of our server

- Support multi-server:
  - Mainly to make it useable outside of our server
  - Make it actually useable for others

## How to build

You will need at the minimum **JDK 17** or later to be able to build this project.

At the root of the project, use the following command:
`./gradlew shadowJar`

Once the build process is finished, the output will be present in `./build/libs/JosetaBot.jar`.

## How to run

You will first need to follow the instruction in the `secret_template.cfg` file before being able to run the bot.

After this, starting the bot is very easy. You just need to execute this command: `java -jar build/libs/JosetaBot.jar`

The following arguments (after the `.jar`)  are available to use:

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