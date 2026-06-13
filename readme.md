# Novel Grabber for wbnovel

## Overview
Novel Grabber for wbnovel is a Java-based ui designed to efficiently download novels from webnovel. It handles authentication, session management, and patches the deprecated repo that currently exists.

## Requirements

### Dependencies
- **Maven** - Build automation tool
- **JDK 8.0 or higher** - Java Development Kit

### Configure Authentication Cookies
The tool requires valid browser cookies to authenticate with wbnovel:

- Open Developer Tools (F12 or Ctrl+Shift+I)
- Navigate to the Network tab
- Reload the page and look for any GET request with status 200 that references a wbnovel URL
- Click on that request and copy all the headers
- Use the formatting example in cookies.example.txt as a template
- Format the headers accordingly and save them to cookies.txt

let AI handle this section when needed.

## Installation & Setup

```bash
git clone https://github.com/sm43576/Novel-Grabber-for-wbnovel.git
cd Novel-Grabber-for-wbnovel

mvn clean install
mvn compile

```
Then execute the JAR file directly after building.

Credits
Original Author: flameish - Original Novel Grabber implementation
Cloudflare Patches: sm43576 - Added patches and fixes for Cloudflare
