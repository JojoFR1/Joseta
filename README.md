# Joseta

![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/JojoFR1/Joseta/build.yml?logo=githubactions?style=for-the-badge)

## Project

Joseta is a multi purpose, single server, Discord bot made with the [JDA](https://github.com/discord-jda/JDA) library.

### Why single server ?

This bot original purpose is to be used in the official [Mindustry France](https://discord.com/invite/hzGPWhZSGV) Discord server only, and so it was made without any plans to support multiple servers nor to be a public bot. Features are also only made based of our needs as of now.

This bot is not *yet* fully usable for personnal use due to multiple hardcoded values, but it is in our plan to make it usable outside of our server. If you wish to use it, we won't give any support for now.

### Why Java and JDA?

We had the choices between JavaScript, Python or Java. Due to personnal preferences, we chose Java.

## How to build

You will need **JDK 17** or later to be able to build this project.

At the root of the project, use:
`./gradlew shadowJar`

Once the build process is finished, the output will be present in `./build/libs/JosetaBot.jar`

## How to run

Running the bot is easy. You just need to execute this command: `java -jar build/libs/JosetaBot.jar`

You have the following arguments (after the `.jar`) available to use:

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