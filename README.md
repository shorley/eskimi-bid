## Bid Server

To start the server, simply run the EskimiBid main scala class.

This will start the server process on http://localhost:8088/api/bid

The server utilizes the DataGenerator scala class to randomly generate sample campaigns to be validated against an incoming Bid request.
You may choose to modify the parameters to randomly generate sample campaigns as follows.

1. Total number of random campaigns to generate for the current session
2. Optional use fixed/ static country or generate randomly from a pre-defined list
3. Price range for the campaigns to be generated
4. Optional use startHour and endHour that this campaign is valid for.

This can all be altered by changing the parameter passed into the call: 
    
    implicit val campaigns = DataGenerator.samplecampaigns


## Bid Tester/ Client

To run/ test the bid server with data, simply run the TestSender main scala class.

This program will randomly generate bids and send an http POST request to the main server running at http://localhost:8088/api/bid

This also uses the DataGenerator class to randomly generate sample bid requests.

Options available for customization include:

1. Number of Impressions for the Bid request
2. Optional use of fixed country or random selection from a predefined list
3. Bidfloor price range to be generated

Customization is done by changing parameters in method call:

    implicit val bidRequest = DataGenerator.samplebid

Once the campaign is started, you may choose to run the Bid client multiple times to see the effect of passing different sample Bid requests and the server responses to matches.

## Changes after fork

Original project: https://github.com/shorley/eskimi-bid

### scalaFmt

One of the things I always do when I start a project is add some plugin to the project for code style. 
I will add scalafmt: https://scalameta.org/scalafmt/.
After that new task will be available, for example:

- scalafmtCheck
- scalafmt
- scalafmtAll

### .gitignore file (updated)

Just an update to the file.