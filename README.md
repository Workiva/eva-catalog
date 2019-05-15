# eva-catalog [![Clojars Project](https://img.shields.io/clojars/v/com.workiva.eva.catalog/client.alpha.svg)](https://clojars.org/com.workiva.eva.catalog/client.alpha) [![CircleCI](https://circleci.com/gh/Workiva/eva-catalog/tree/master.svg?style=svg)](https://circleci.com/gh/Workiva/eva-catalog/tree/master)

<!-- toc -->

- [Overview](#overview)
- [Definitions](#definitions)
- [API Documentation](#api-documentation)
  * [Client](#client)
  * [Server](#server)
  * [Common Lib](#common-lib)
- [Maintainers and Contributors](#maintainers-and-contributors)
  * [Active Maintainers](#active-maintainers)
  * [Previous Contributors](#previous-contributors)

<!-- tocstop -->

## Overview

The eva-catalog service exists to provide a central repository (and client)
for handling the configuration maps used to connect to Eva.

## Definitions

For now, the catalog service holds and distributes fully contained flat
configuration maps. These maps are keyed by three values: the *tenant*,
the *category*, and the *label*.

The *tenant* for a configuration is a partitioning of ownership for
configurations.

The *category* for a configuration indicates a cross-configuration
coupling. Databases that will require cross-database queries should
share a common category.

The *label* on a configuration is a name that, along with the *tenant*
and *category* uniquely identifies a configuration for a specific
database.

## API Documentation

### Client

[Clojure API documentation can be found here.](/documentation/client.alpha/clojure/index.html)
[Java API documentation can be found here.](/documentation/client.alpha/java/index.html)

### Server

[Clojure API documentation can be found here.](/documentation/server.alpha/clojure/index.html)

### Common Lib

[Clojure API documentation can be found here.](/documentation/common.alpha/clojure/index.html)

## Maintainers and Contributors

### Active Maintainers

-

### Previous Contributors

- Ryan Heimbuch <ryan.heimbuch@workiva.com>
- Houston King <houston.king@workiva.com>
- Tyler Wilding <tyler.wilding@workiva.com>
