# AI Usage and Reflection

## 1. How Did You Use AI in This Assignment?

- I used AI mainly as a development assistant and reference while building the treasure hunt version of the PlaceBook ideas.
- AI helped me create a starting structure for the map screen, Room database, RecyclerView, and the logic that unlocks the next location after the current one is visited.
- One example was asking AI how I could change a normal place-bookmarking app into a sequential treasure hunt. I kept the general idea but adapted the data model so each place has an order number, a clue, and a visited status.
- Another example was the location-permission code. I used the suggested pattern as a reference, then checked where the permission result affected the Google Map before using it.
- For concepts that were newer to me, especially connecting Room data with the treasure-hunt progress, I compared the AI explanation with the PlaceBook tutorial pattern and Android documentation.

## 2. How Did You Understand, Verify, and Adapt the Code?

- I verified the code by running the app, checking that the map loaded, testing the location permission, opening the list and detail screens, and marking locations as visited one at a time.
- I also traced the treasure-hunt logic to make sure a participant cannot skip directly to a later stop. Only the current unlocked location can be completed.
- One important change was using a `huntOrder` value for every place. This makes the sequence simple to understand and lets the app calculate the next stop.
- I also stored `isVisited` in Room instead of only keeping it in memory. I did this so progress is still there after closing and reopening the app.

## 3. What Did You Learn or Get Better At Through This Work?

- I got more comfortable understanding how a database, an adapter, activities, and a map can all work together instead of thinking about each feature separately.
- I improved my understanding of Room because I could see why an Entity, DAO, and Database class each have a different job.
- I also got better at following program flow. For example, marking one place as visited updates the database, changes the progress counter, and reveals the next marker.
- What went well was breaking the app into smaller pieces and giving each class one main responsibility.
- The part that was more difficult was handling map permissions and keeping the UI synchronized with the saved progress. Testing each feature separately made that easier to understand.
