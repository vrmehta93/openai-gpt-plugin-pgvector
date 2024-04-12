# OpenAI GPT Plugin With Spring AI and PostgreSQL pgvector

Follow this guide to create and launch a custom OpenAI GPT plugin using Spring AI and PostgreSQL pgvector.

The plugin will turn ChatGPT into a movie sommelier - a fully autonomous AI agent that provides you with movie recommendations and updates your movies catalogue.

![main-picture](https://github.com/YugabyteDB-Samples/openai-gpt-plugin-pgvector/assets/1537233/998a9763-c455-4973-b87a-20cdeec443be)

The AI will use Postgres and its pgvector extension to retrieve information from a custom movies databases. Then, ChatGPT will be able to update your own movie catalogue with various movies selection.

## Prerequisites

1. [ChatGPT account](http://chat.openai.com)
2. [OpenAI API key](https://platform.openai.com)
3. Java 21+ and Maven.
4. [Heroku account](https://dashboard.heroku.com/apps)
5. [YugabyteDB Managed](http://cloud.yugabyte.com/) or another Postgres distribution that supports the pgvector extension.

## Provision Postgres With pgvector

The ChatGPT knowledgbase will be augmented with the information stored in Postgres. The AI agent will be querying or updating data stored in Postgres by following a conversation with the users.

Deploy an instance of Postgres distribution that supports the pgvector extension. The database has to be accessable to an application backend that will be deployed on Heroku.

In this guide, we're using YugabyteDB - a distributed database built on Postgres. With YugabyteDB, the OpenAI GPT plugin will be able to scale read with write workloads and tolerate possible outages in the cloud.

Create a single-node instance or multi-node [YugabyteDB Managed cluster](https://docs.yugabyte.com/preview/yugabyte-cloud/)

## Deploy Application Backend to Heroku

Deploy the application backend to Heroku:

1. Go to the backend directory:

    ```shell
    cd backend/
    ```

2. Build the backend:

    ```shell
    mvn clean package -DskipTests
    ```

3. Log in to your Heroku account:

    ```shell
    heroku login
    ```

4. Install the Java plugin on Heroku:

    ```shell
    heroku plugins:install java
    ```

5. Create a Heroku application for the backend:

    ```shell
    heroku create openai-gpt-plugin-backend
    ```

6. Provide application and database-specific configuration settings to Heroku:

    ```shell
    heroku config:set PORT=80 -a openai-gpt-plugin-backend
    heroku config:set OPENAI_API_KEY=<YOUR_OPEN_AI_API_KEY> -a openai-gpt-plugin-backend
    heroku config:set BACKEND_API_KEY=OpenAIGPTPlugin -a openai-gpt-plugin-backend

    heroku config:set DB_URL="<YOUR_DB_URL>" -a openai-gpt-plugin-backend
    heroku config:set DB_USER=<YOUR_DB_USER> -a openai-gpt-plugin-backend
    heroku config:set DB_PASSWORD=<YOUR_DB_PWD> -a openai-gpt-plugin-backend
    ```
    where:

    * `BACKEND_API_KEY` - a custom key that the ChatGPT will be using to authenticate with the application backend.
    * `DB_URL` - a connection endpoint to a Postgres instance. If you use YugabyteDB Managed, the the URL format should be as follows: `jdbc:postgresql://{YGABYTEDB_NODE_ADDRESS}:5433/yugabyte?ssl=true&sslmode=require`

8. Deploy the application to Heroku:

    ```shell
    heroku deploy:jar target/yugaplus-backend-1.0.0.jar -a openai-gpt-plugin-backend
    ```

Once deployed, check the application logs to ensure it's started without failures and managed to connect to your database.

```shell
heroku logs --tail -a openai-gpt-plugin-backend
```

Test the application by sending the following HTTP request with HTTPie tool:

```shell
http GET https://{YOUR_APP_URL_ON_HEROKU}/api/movie/search prompt=="A long time ago in a galaxy far, far away..." X-Api-Key:OpenAIGPTPlugin
```

## Configure Custom Domain and SSL Certificate

ChatGPT requires you to register and use a custom verified domain for GPT plugins. Also, you need to configure an SSL certificate on Heroku for your applicaion.

Overall, you need to:

* Register a custom doman
* Create an SSL certificate for your app on Heroku (Heroku supports the automatically managed certificates)
* Use the custom domain for your Heroku deployment.
* Verify your OpenAI builder profile and custom domain with ChatGPT.

[This video](https://youtu.be/Ysh9dwia8FM?t=251) shows how to perfom these steps using GoDaddy as a DNS provider.

Once everything is set up properly, validate that the following API call executes succesfully replacing the `YOUR_CUSTOM_DOMAIN` with your domain:

```shell
http GET https://{YOUR_CUSTOM_DOMAIN}/api/movie/search prompt=="A long time ago in a galaxy far, far away..." X-Api-Key:OpenAIGPTPlugin
```

## Creating OpenAI GPT Plugin

Next, let's create a custom GPT plugin and deploy it on the [OpenAI GPT Store](https://openai.com/blog/introducing-the-gpt-store).

Note, as a custom GPT builder, you'll be able to earn if your custom GPT gets traction on the marketplace. This is what OpenAI says:

*In Q1 we will launch a GPT builder revenue program. As a first step, US builders will be paid based on user engagement with their GPTs. We'll provide details on the criteria for payments as we get closer.*

Your plugin will turn ChatGPT into a movie sommelier - a fully autonomous AI agent that provides you with movie recommendations and updates your movies catalogue.

To achieve that, ChatGPT uses [actions](https://platform.openai.com/docs/actions/introduction) that let the LLM connect to your application and access APIs that are defined in a configuration file following the [OpenAPI Specification](https://swagger.io/specification/).

Follow the steps below to create a plugin for the movie sommelier or watch the video (TBD).

First, create a custom GPT plugin and configure the actions:

* Go to your [custom GPTs](https://chat.openai.com/gpts/mine)
* Start configuring the plugin naming it **Movie Sommelier**

    ![1_start_configuring](https://github.com/YugabyteDB-Samples/openai-gpt-plugin-pgvector/assets/1537233/f97bf688-bd61-4fca-9536-14d2c0f421b3)

Next, create configure a new action:

* Open the provided `gpt-spec.yaml` file and change the `server.url` property to the name of your custom domain name that points out to the Heroku application.

* Start configuring a new action pasting the contents of the `gpt-spec.yaml` file into the **schema** area:
   
    ![2-configure-action-specification](https://github.com/YugabyteDB-Samples/openai-gpt-plugin-pgvector/assets/1537233/e8f0c876-268a-4c43-8d3a-39c6ca813339)

* Click the **Authentication** menu and configure the **API Key** authentication method (use `OpenAIGPTPlugin` as a value for the **API Key** field):
    
    ![3-configure-authentication](https://github.com/YugabyteDB-Samples/openai-gpt-plugin-pgvector/assets/1537233/12e7fc8c-4337-4598-adc1-7b7622ae49e7)

* Click the **Test** button for one API endpoints making sure ChatGPT can make a call to the Heroku app:

    ![4-test-actions](https://github.com/YugabyteDB-Samples/openai-gpt-plugin-pgvector/assets/1537233/dcf071e4-5c22-47ae-8c50-5da62e2d3913)

Then, go to the **GPT Builder** screen and provide the following input. ChatGPT will take this input a generate instructions for the plugin.

```text
You are a world-famous movie critic who can easily come up with movie recommendations even for the most demanding users. You're using the provided Action that draws information from a third-party service. 

Additionally, you can assist users in viewing their movie catalogue and updating it by adding or removing movies.

If a user asks to add or remove a movie from the catalogue, double-check first to ensure they really want to proceed with the update. If the user becomes annoyed by your requests for permission to update the catalogue, stop asking for permission and proceed with the updates.
```


![5-generate-instructions](https://github.com/YugabyteDB-Samples/openai-gpt-plugin-pgvector/assets/1537233/4f718d27-e10b-4d12-b263-303b5a4d1cdf)

After that, the click on the **Configure** screen to see the final instructions, conversation starts and other details. For instance, the final instructions might be as follows:

```text
As a world-famous movie critic, I use my vast knowledge and the third-party plugin to provide personalized movie recommendations and assist users with managing their movie catalogues. I can recommend movies, view user libraries, and help add or remove movies from their catalogues. I always confirm with users before making changes to their catalogue, unless they request to skip confirmation steps.
```

![6-final-configuration-screen](https://github.com/YugabyteDB-Samples/openai-gpt-plugin-pgvector/assets/1537233/8f7905cd-7c94-4a8c-8c2f-47edf3e38be5)

Finally, click the **Create** button and launch the GPT plugin:

![7-launch-gpt](https://github.com/YugabyteDB-Samples/openai-gpt-plugin-pgvector/assets/1537233/7e2905b9-9ca5-4913-ad1f-2ef617bcb840)

## Play With Plugin

Go ahead and give a try to the movie sommelier! Ask for some movie recommendations, aks to show your personal movie catalogue and don't hesitate asking to add or remove movies from your own collection.

![8-play-with-plugin](https://github.com/YugabyteDB-Samples/openai-gpt-plugin-pgvector/assets/1537233/7b827ee0-4658-4289-bb68-13d7e33dc61c)


