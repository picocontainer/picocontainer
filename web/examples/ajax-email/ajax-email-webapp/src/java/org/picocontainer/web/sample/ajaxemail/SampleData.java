package org.picocontainer.web.sample.ajaxemail;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.picocontainer.web.sample.ajaxemail.persistence.Persister;

public class SampleData {

    private final Persister persister;
    private final UserStore userStore;

    static final User GILL_BATES = new User("Gill Bates", "1234");
    static final User BEEVE_SALMER = new User("Beeve Salmer", "1234");

    private List<Message> messages = new ArrayList<Message>();
    {
        messages.add(new Message("J Query", GILL_BATES.getName(), "Nice Example", "Very nice example application you've created", false, time()));
        messages.add(new Message("Needie Joe", GILL_BATES.getName(), "Give me Money!", "You're one of the most rich people in the world, help me out", false, time()));
        messages.add(new Message("LotteryWinner", GILL_BATES.getName(), "You've Won the Lottery", "Just send us $2000 and we'll send you the money.", false, time()));
        messages.add(new Message("Barbara Smith", GILL_BATES.getName(), "Leaving Early on Friday", "I have to take my son to the doctor.", false, time()));
        messages.add(new Message("Amy Jones", GILL_BATES.getName(), "Status Report", "Please find my status report for the week here.", false, time()));
        messages.add(new Message("Instant Millionaire", GILL_BATES.getName(), "HOT STOCK TIP!!", "Buy XWFX, Buy XWFX, Buy XWFX!", false, time()));
        messages.add(new Message("R. Benjamin Graham IV", GILL_BATES.getName(), "Meeting Moved to 4pm", "", false, time()));
        messages.add(new Message("Trudy Barker", GILL_BATES.getName(), "Coming in Late", "Not that you'll ever notice", false, time()));
        messages.add(new Message("Sammy Shaggs", GILL_BATES.getName(), "Lunch?", "Let's Do Lunch at McDonald's - you can buy!", false, time()));
        messages.add(new Message("Kate Robertson", GILL_BATES.getName(), "When are our raises coming?", "I need more money", false, time()));
        messages.add(new Message(GILL_BATES.getName(), "Jeeves Sobs", "Nice OS", "You've made a great OS there Jeeves", false, time()));
        messages.add(new Message("Parah Salin", BEEVE_SALMER.getName(), "Job", "I need a job pls.", false, time()));
        messages.add(new Message(BEEVE_SALMER.getName(), "Jeeves Sobs", "Rubbish OS", "Your OS is just a joke", false, time()));
    }

    private long time;

    public SampleData(Persister pm, UserStore userStore) {
        this.persister = pm;
        this.userStore = userStore;
        time = System.currentTimeMillis();
    }

    private long time() {
        time = time - 1800000;
        return time;
    }

	public void load() {

        User gill = userStore.getUser(GILL_BATES.getName());
        if (gill == null) {
            gill = GILL_BATES;
            makePersistent(gill);
        }
        User beeve = userStore.getUser(BEEVE_SALMER.getName());
        if (beeve == null) {
            beeve = BEEVE_SALMER;
            makePersistent(beeve);
        }

        Query query = persister.newQuery(Message.class, "id > -1");

        for (Object message : (Collection<?>) new ArrayList((Collection<?>) query.execute(null))) {
            persister.deletePersistent(message);
        }

        for (Message message : messages) {
            persister.makePersistent(message);
        }

    }

    private void makePersistent(Object obj) {
        persister.makePersistent(obj);
    }
}
