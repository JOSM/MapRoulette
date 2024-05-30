README 
======

# Usage
## Download MapRoulette Tasks
There are two methods to download tasks from MapRoulette.
1. From the JOSM `Download data` window (`OpenStreetMap data`, `Raw GPS data`, `Notes`, ..., `MapRoulette Tasks`)
2. From the `MapRoulette Tasks` toggle dialog (`Download Data`) which downloads data in the current viewport

## Workflow
1. Select a MapRoulette task in the mapview *or* in the `MapRoulette Tasks` window
2. Click on `Start Task` -- this will "lock" the task on MapRoulette until you finish and click on `Stop Task`.
   This helps ensure that you aren't mapping something concurrently with someone else.
3. Perform the task. Instructions can be viewed in the `Current MapRoulette Task` toggle dialog.
4. In `Current MapRoulette Task`, select the appropriate action (`False Positive`, `Too hard/Cannot see`, `Fixed`,
   `Already Fixed/Not an issue`, or `Skipped`; some options are hidden in drop-down menus).
5. Uploading a changeset or `Stop Task` will then upload the task status.

If a task specifies an OSM primitive

# Author
* Taylor Smock <tsmock@meta.com>

# License
GPLv3 or any later version