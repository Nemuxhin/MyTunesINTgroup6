06-12 Push Notes
Grud all finished for playlist and songs
also improved library so that it only runs mp3 and mvk files
im working on fixing the playlist play logic now and that should hopefully be done tommorow.
James :)

10-12 Push Notes
Final Polish added including major function improvement
implemented changes:
Volume Slider
Song Progression and interactive bar
Searching logic for songs
Minor Visual flare to hello view

Everything now works fully it is ready for the assignment.
Minor changes will be made tomorrow ragarding adding comments to code
PLZ TEST WHENEVER U CAN :)

I WILL START TO ADD CODE SNIPPETS TOMORROW TO THE ESSAY!

12-12-2025 Push Notes (Samu)
I applied only the remaining fixes needed to make the project fully align with the MyTunes requirements, without changing your existing structure or removing any of your comments.
Main changes:
- Added a proper Business Logic Layer (BLL): introduced MyTunesManager and routed GUI actions through it (GUI -> BLL -> DAL).
- Fixed Delete Song DB integrity: deleting a song now also removes PlaylistSong relations first (transaction-based) so the database stays clean.
- Persisted playlist ordering: Move Up/Down now updates SongIndex in the PlaylistSong table so the order survives app restart.
- Fixed Edit Song: time and file path are now saved correctly when updating an existing song.
- Updated DB seed: removed audio paths that were not present in the data folder to prevent playback errors.

Testing note:
I tried to run the Maven wrapper build here, but the wrapper downloads Maven from the internet; this environment blocks network access, so it fails with a "connection refused". On your PC you can verify with: ./mvnw -DskipTests package
