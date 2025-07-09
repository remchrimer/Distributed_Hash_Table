
<a id="readme-top"></a>
<br />
<div align="center">

<h3 align="center">CS4730 Distributed Systems</h3>

  <p align="center">
    Hyeonwoo Lee : Programming Assignment 5<br />
    <br />
    <br />

  </p>
</div>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#getting-started">Instructions</a></li>
      </ul>
    </li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

<!-- ABOUT THE PROJECT -->

## About The Project

In this lab, you will implement a DHT like CHORD and store and retrieve objects from it. All
peers and objects are identified by IDs from 1 to 127. (You do not need to apply the hash function
as in CHORD, you can use their IDs directly).
You can assume that there is a bootstrap server that maintains the ring as peers join, and can
tell each peer where their place is in the ring. The bootstrap server is not doing any monitoring of
the peers and the peers do not implement any heartbeat service.
Each peer will know the name of the bootstrap server from a command line argument. Each
peer maintains the predecessor and the successor peer in the ring. Each peer also maintains a file
with stored objects. You can assume that each peer will have a pre-populated object store. The
file consists of lines looking like this: clientID::objectID
Each peer starts by contacting the bootstrap server. The bootstrap server maintains the ring,
thus can tell the joining peer immediately where their place is in the ring. The bootstrap server
will inform each of the peers from the ring that need to update their predecessor and the successor
in the ring, i.e., the peer before and the peer after the insertion point.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Built With

![Java][Java-url]

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- GETTING STARTED -->

## Getting Started
### Instructions

Building:

- Use the following commands in your terminal or command prompt:

  ```sh
    docker build . -f BootstrapDockerfile -t prj5-bootstrap
    docker build . -f PeerDockerfile -t prj5-peer
    docker build . -f ClientDockerfile -t prj5-client
  ```

Orchestration:
- Use the following commands in your terminal or command prompt:

  ```sh
    docker docker compose -f docker-compose.yml up
  ```

<p align="right">(<a href="#readme-top">back to top</a>)</p>


## Acknowledgments
- This template is adapted from the [Best-README-Template](https://github.com/othneildrew/Best-README-Template) repository.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[Java-url]: https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white