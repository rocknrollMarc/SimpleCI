module bot.core;

import irc.collections;
import std.string;
import std.array;
import irc.irc;

private shared(IRCBot) bot;
private shared(string) commandPrefix;
private shared(List!string) admins;

public class BotCore {

    public this(string nick, string user, string realn, 
                                                    string adr, short port, string comPref) {
        auto _bot = new IRCBot(new IRCConfig(nick, user, realn, 
                                                    getAddress(adr, port)[0]));
        commandPrefix = cast(shared) comPref;
        admins = cast(shared) new List!string;

        _bot.readyEventHandler.register(&onReady);
        _bot.messageEventHandler.register(&onMessage);

        bot = cast(shared) _bot;
    }

    public void connect() {
        (cast(IRCBot) bot).connect();
    }

    public void addAdmin(string user) {
        (cast(List!string) admins) += user;
    }

    public bool loop() {
        return (cast(IRCBot) bot).loop();
    }

    public bool isInChannel(string chan) {
        return (cast(IRCBot) bot).isInChannel(chan);
    }

    public void join(string chan) {
        (cast(IRCBot) bot).join(chan);
    }

    public void msg(string chan, string _msg) {
        auto ch = (cast(IRCBot) bot).getChannel(chan);
        if (ch)
            ch.sendMessage(_msg);
    }

    public bool isAdmin(string user) {
        return (cast(List!string) admins).contains(user);
    }

    public void disconnect() {
        if (bot)
            (cast(IRCBot) bot).disconnect();
        destroy(bot);
        bot = null;
    }
}

extern (C) {
    void status(const(char)*, const(char)*);
    void build(const(char)*, const(char)*);
    void listJobs(const(char)*);
    void loadJobs(const(char)*);
    void postReadyEvent();
}

/* Event functions */
private void onReady(ReadyEvent e) {
    postReadyEvent();
}

private void onMessage(MessageEvent e) {
    string prefix = cast(string) commandPrefix;
    if (!((e.message.length > prefix.length) && (e.message[0 .. prefix.length] == prefix)))
        return; // Not a command
    immutable string message = e.message[1 .. e.message.length];
    string[] args = message.split();

    switch (args[0]) {
        case "listJobs":
            listJobs(toStringz(e.target.target));
            break;
        case "status":
            string jobName = args.length >= 2 ? args[1] : null;
            status(toStringz(e.target.target), jobName != null ? toStringz(jobName) : null);
            break;
        case "loadJobs":
            if (!(cast(List!string) admins).contains(e.sender.user)) {
                e.target.sendMessage("> You must be admin to use this command.");
                return;
            }
            loadJobs(toStringz(e.target.target));
            break;
        case "build":
            if (!(cast(List!string) admins).contains(e.sender.user)) {
                e.target.sendMessage("> You must be admin to use this command.");
                return;
            }
            string jobName = args.length >= 2 ? args[1] : null;
            build(toStringz(e.target.target), jobName != null ? toStringz(jobName) : null);
            break;
        case "help":
            auto commands = [
                "listJobs": "Lists Jobs",
                "status": "Displays Job Statuses",
                "loadJobs": "Loads Jobs",
                "build": "Builds a Job",
                "help": "Displays Help"
            ];
            string command = args.length >= 2 ? args[1] : null;
            if (command != null && command in commands) {
                e.target.sendMessage(format("> %s: %s", command, commands[command]));
            } else {
                auto cmdstring = ">";

                foreach(cmd; commands.keys) {
                    if (cmd == "help") {
                        cmdstring = format("%s %s", cmdstring, cmd);
                    } else {
                        cmdstring = format("%s, %s", cmdstring, cmd);
                    }
                }

                e.target.sendMessage(cmdstring);
            }
            break;
        default:
    }
}