![](https://media-exp1.licdn.com/dms/image/C4D1BAQFXbFOCkyU6_Q/company-background_10000/0/1612543717706?e=2147483647&v=beta&t=uMxx0Lx8R-t3Fglk10B_hbF_KvMYf87EJvoqUdtsRpQ)

# **Cloud Computing Systems** <br> Auctions Project </span><br>
### Repository: [https://github.com/goncalovirginia/CCS](https://github.com/goncalovirginia/CCS)<br>
### Gonçalo Virgínia - 56773 - g.virginia@campus.fct.unl.pt<br>
### André Correia - 64783 - aas.correia@campus.fct.unl.pt<br>
### Rodrigo Fontinha - 64813 - r.fontinha@campus.fct.unl.pt<br>

<div style="page-break-after: always"></div>

## 1. Introduction

Main project for the Cloud Computing Systems course, comprised of the backend implementation of a scalable auction system akin to EBay.

Using Azure as the cloud provider, the project is provisioned with both a platform for application deployment in the Azure cloud and features that support the project implementation process and allow for more efficient programming.

## 2. Structure

This project is separated into two components, the **auction.project**, consisting of the main application with which users can interact with, and **fun.project**, an auxiliary service employing Azure Functions, that has extremely useful trigger integrations with other Azure services, to easily manage or build upon them.

Among the various globally available services that Azure provides, this project aims to take advantage of the following:

* **Storage Account** to manage any media files published to the application, namely images;
* **CosmosDB** as the database system to store and manage data;
* **Azure Functions** to run snippets of serverless code;
* **Azure Cognitive Search** to reduce the complexity of importing data for immediate consumption via indexes.

Additionally, **Redis** was also integrated in the project to increase its overall performance when in comes to response time, while also reducing the amount of processing done by the application and database service.

### 2.1 Auctions App

The auction application component - **auction.project** - includes the backend that corresponds to the project and the test scripts in the format of *yml* files.

The backend was divided into five different modules that interact with one another to allow the use of the services offered by the application. These can be represented as in the following table:

| Module | Role |
| --- | --- |
| **cache** | Communication layer with Redis as the caching system |
| **data** | Data structures and communication layer with CosmosDB as the database system |
| **mgt** | Automated management of Azure resources and corresponding access keys |
| **srv** |  Definition layer for endpoints interacting with the application services |
| **utils** | Storage and availability of auxiliary properties to the application |

The *yml* files use **Artillery** to run different test scenarios and ensure the application behaviour is correct and just. The scenarios make it possible to organize a set of tests defined on data and application endpoints, making the entire process of testing the developed code a little easier.

<div style="page-break-after: always"></div>

### 2.2 Functions App

The second component of the project - **fun.project** - was developed in a Maven project separate to that of the auction project and uses the Azure Functions service to incorporate serverless functions in the application.

Much like the previous component, the functions application
includes a group of modules. However, these modules were reduced to the resources that are imperative to run the functions:

| Module | Role |
| --- | --- |
| **data** | Data structures |
| **dblayer** | Communication layer with CosmosDB as the database system |
| **pt** | Serverless functions |
| **rediscache** |  Communication layer with Redis as the caching system |
| **utils** | Storage and availability of auxiliary properties to the application |

The serverless functions allow the execution of small code snippets that have some utility towards the application, as the following table suggests:

| Function | Trigger | Utility |
| --- | --- | --- |
| **CloseAuctions** | Timer | Automatically closes an auction, at midnight, in case its auction lifetime has passed |
| **Thumbnail** | Blob | Creates a 720p resolution thumbnail of any *jpg* file created or updated to the images container |

Observations:

* **CloseAuctions**
> The data property that determines the end of an auction's lifetime is a date, not including time. This means the function can be triggered daily instead of every couple of seconds, minutes or hours, significantly reducing the amount of queries the database must compute.

* **Thumbnail**
> The thumbnails are stored in a separate thumbnails container and the 720p compression is performed assuming it is the desired resolution for the images used in the application. Only works for *jpg* files.

<div style="page-break-after: always"></div>

## 3. Evaluation

As requested in the project handout, **Artillery** scripts were configured to test and evaluate the application performance under different settings.

Firstly, the application was deployed in the West Europe region, with caching (see Annex 1).

![Results 1](...)

Secondly, the application was deployed in the West Europe region still, but without caching (see Annex 2).

![Results 2](...)

Lastly, the application was deployed in the US West region, outside of Europe, with caching (see Annex 3).

![Results 3](...)

Given the results of running the above mentioned scripts, it is fair to assume to application performs [...].

<div style="page-break-after: always"></div>

## Annexes

Annex 1
```yml

```

Annex 2
```yml

```

Annex 3
```yml

```