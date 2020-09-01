package net.sunyounglee.javajokes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Joker {
    public String getJoke() {
        List<String> jokeCollection = new ArrayList<>();
        jokeCollection.add("What do you call a fish without eyes? Fsh.");
        jokeCollection.add("Why did the scarecrow win an award? Because he was outstanding in his field.");
        jokeCollection.add("Why shouldn’t you write with a broken pencil? Because it’s pointless.");
        jokeCollection.add("Where can you buy chicken broth in bulk? The stock market.");
        jokeCollection.add("Why did the picture go to jail? Because it was framed.");
        Collections.shuffle(jokeCollection);
        return jokeCollection.get(0);
    }
}