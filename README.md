Some examples of real life applications that uses areays includes;
Digital Images: Storing the color of every pixel in a grid.
Seat Booking Systems: Managing rows and columns of seats for flights or cinemas.
Board Games: Tracking the positions of pieces on a chessboard or tic-tac-toe grid.
Scoreboards: Storing lists of top scores or player statistics in order.

Why Arrays are the Best Choice for Your Spreadsheet
For the "Simple Arithmetic Sheet," using a 2D array is much better than using other complex data structures for a few simple reasons:

1. It perfectly matches the visual grid
A spreadsheet is naturally a grid of rows and columns, and a 2D array is exactly that. It makes translating user input very easy. For example, cell "A1" perfectly translates to the very first slot in your array (row 0, column 0).

2. Instant speed and lookups
When a user types a formula like =A1+B1, the backend needs to find those values immediately. Because arrays are highly organized in the computer's memory, the program can instantly grab the data from those exact spots without having to "search" for it.

3. The size is fixed
Using a specific size (8 rows by 5 columns). Arrays are  perfect for this because you create them with a strict size right from the start. This keeps the code incredibly simple and uses the computer's memory very efficiently, whereas other data structures that grow and shrink would add unnecessary complexity.
