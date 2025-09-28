<a name="readme-top"></a>
<!-- Template Credit: Othneil Drew (https://github.com/othneildrew),
                      https://github.com/othneildrew/Best-README-Template/tree/master -->
<!-- PROJECT SHIELDS -->
<div align="center">

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![LinkedIn][linkedin-shield]][linkedin-url]

</div>

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <h1>okhttp-client-extensions</h1>

  <p align="center">
    A set of abstractions to support okhttp-based Java clients.
    <br />
    <a href="https://www.amilesend.com/okhttp-client-extensions"><strong>Maven Project Info</strong></a>
    -
    <a href="https://www.amilesend.com/okhttp-client-extensions/apidocs/index.html"><strong>Javadoc</strong></a>
    <br />
    <a href="https://github.com/andy-miles/okhttp-client-extensions/issues">Report Bug</a>
    -
    <a href="https://github.com/andy-miles/okhttp-client-extensions/issues">Request Feature</a>
  </p>
</div>


<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#usage">Usage</a>
      <ul>
        <li><a href="#getting-started">Getting Started</a></li>
        <li><a href="#recipes">Recipes</a></li>
      </ul>
    </li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>

<!-- ABOUT THE PROJECT -->
# About The Project

A set of abstractions to support okhttp-based Java clients.

Patterns include:
* Defining an AuthManager that is responsible for including the authorization information in requests
* GsonParser interface to support JSON-based response body marshalling
* Encapsulating Connection to simplify invoking HTTP based requests and leveraging GsonParsers to marshall to desired POJO types
* Helpers to simplify encryption and KeyStore management

It currently supports the following clients:
* [onedrive-java-sdk](https://github.com/andy-miles/onedrive-java-sdk)
* [tmdb-java-client](https://github.com/andy-miles/tmdb-java-client)
* [omdb-java-client](https://github.com/andy-miles/omdb-java-client)
* [tvmaze-java-client](https://github.com/andy-miles/tvmaze-java-client)
* [discogs-java-client](https://github.com/andy-miles/discogs-java-client)


<div align="right">(<a href="#readme-top">back to top</a>)</div>

<!-- CONTRIBUTING -->
## Contributing

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/MyFeature`)
3. Commit your Changes (`git commit -m 'Add my feature'`)
4. Push to the Branch (`git push origin feature/MyFeature`)
5. Open a Pull Request

<div align="right">(<a href="#readme-top">back to top</a>)</div>

<!-- LICENSE -->
## License

Distributed under the MIT license. See [LICENSE](https://github.com/andy-miles/okhttp-client-extensions/blob/main/LICENSE) for more information.

<div align="right">(<a href="#readme-top">back to top</a>)</div>


<!-- CONTACT -->
## Contact

Andy Miles - andy.miles (at) amilesend.com

Project Link: [https://github.com/andy-miles/okhttp-client-extensions](https://github.com/andy-miles/okhttp-client-extensions)

<div align="right">(<a href="#readme-top">back to top</a>)</div>


<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/andy-miles/okhttp-client-extensions.svg?style=for-the-badge
[contributors-url]: https://github.com/andy-miles/okhttp-client-extensions/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/andy-miles/okhttp-client-extensions.svg?style=for-the-badge
[forks-url]: https://github.com/andy-miles/okhttp-client-extensions/network/members
[stars-shield]: https://img.shields.io/github/stars/andy-miles/okhttp-client-extensions.svg?style=for-the-badge
[stars-url]: https://github.com/andy-miles/okhttp-client-extensions/stargazers
[issues-shield]: https://img.shields.io/github/issues/andy-miles/okhttp-client-extensions.svg?style=for-the-badge
[issues-url]: https://github.com/andy-miles/okhttp-client-extensions/issues
[license-shield]: https://img.shields.io/github/license/andy-miles/okhttp-client-extensions.svg?style=for-the-badge
[license-url]: https://github.com/andy-miles/okhttp-client-extensions/blob/master/LICENSE.txt
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://www.linkedin.com/in/andy-miles
[product-screenshot]: images/screenshot.png
[Next.js]: https://img.shields.io/badge/next.js-000000?style=for-the-badge&logo=nextdotjs&logoColor=white
[Next-url]: https://nextjs.org/
[React.js]: https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB
[React-url]: https://reactjs.org/
[Vue.js]: https://img.shields.io/badge/Vue.js-35495E?style=for-the-badge&logo=vuedotjs&logoColor=4FC08D
[Vue-url]: https://vuejs.org/
[Angular.io]: https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white
[Angular-url]: https://angular.io/
[Svelte.dev]: https://img.shields.io/badge/Svelte-4A4A55?style=for-the-badge&logo=svelte&logoColor=FF3E00
[Svelte-url]: https://svelte.dev/
[Laravel.com]: https://img.shields.io/badge/Laravel-FF2D20?style=for-the-badge&logo=laravel&logoColor=white
[Laravel-url]: https://laravel.com
[Bootstrap.com]: https://img.shields.io/badge/Bootstrap-563D7C?style=for-the-badge&logo=bootstrap&logoColor=white
[Bootstrap-url]: https://getbootstrap.com
[JQuery.com]: https://img.shields.io/badge/jQuery-0769AD?style=for-the-badge&logo=jquery&logoColor=white
[JQuery-url]: https://jquery.com 
