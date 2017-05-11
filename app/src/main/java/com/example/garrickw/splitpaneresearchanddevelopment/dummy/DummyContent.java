package com.example.garrickw.splitpaneresearchanddevelopment.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {
    private static final int COUNT = 25;
    private static TaskModel taskModels[];
    private static DummyContent instance;

    public static DummyContent getInstance() {
        if(instance == null) {
            instance = new DummyContent();
        }
        return instance;
    }

    private final String[] loremIpsums = {"Lorem ipsum dolor sit amet, consectetur " +
            "adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliq" +
            "ua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut a" +
            "liquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in volupt" +
            "ate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat c" +
            "upidatat non proident, sunt in culpa qui officia deserunt mollit anim id est lab" +
            "orum.",
            "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut a liq" +
            "uip ex ea commodo consequat.",
            "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium dolor" +
                    "emque laudantium, totam rem aperiam, eaque ip" +
                    "sa quae ab illo inventore veritatis et quasi architecto beatae vitae dic" +
                    "ta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernat" +
                    "ur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratio" +
                    "ne voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsu" +
                    "m quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam" +
                    " eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat v" +
                    "oluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam c" +
                    "orporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? " +
                    "Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse qua" +
                    "m nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo volu" +
                    "ptas nulla pariatur",
            "odit aut fugit, sed quia consequuntur magni dolores eos qui",
            "Sed ut perspiciatis unde omnis iste natus error sit v" +
                    "oluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ips" +
                    "a quae ab illo inventore veritatis et quasi architecto beatae vitae dicta" +
                    " sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur " +
                    "aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione v" +
                    "oluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum qui" +
                    "a dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius " +
                    "modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptat" +
                    "em. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis " +
                    "suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis aute" +
                    "m vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil mo" +
                    "lestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla " +
                    "ariatur",
            "consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro"
    };

    private final String[] titles = {"Apple", "Banana", "Orange", "Pineapple", "Mango",
            "Watermelon", "Strawberry", "Grapes", "Jackfruit", "Carrot", "Fig"};

    private final String[] processes = {"collect", "transfer", "pickup", "refund", "exit",
            "enter", "some words", "a process name", "eat", "drink", "play"};

    public TaskModel[] getDummyData() {
        if(taskModels == null || taskModels.length == 0) {
            taskModels = new TaskModel[COUNT];
            Random rand = new Random();
            for (int i = 0; i < taskModels.length; i++) {
                taskModels[i] = new TaskModel(
                        titles[rand.nextInt((titles.length))],
                        loremIpsums[rand.nextInt((loremIpsums.length))],
                        processes[rand.nextInt((processes.length))]);
            }
        }

        return taskModels;
    }

    public TaskModel getTaskModelForIndex(int index) {
        return getDummyData()[index];
    }
}
