package su.nightexpress.moneyhunters.basic.config;

import su.nexmedia.engine.api.lang.LangKey;

public class Lang {

    public static final LangKey COMMAND_STATS_DESC    = new LangKey("Command.Stats.Desc", "Show your stats.");
    public static final LangKey COMMAND_STATS_USAGE   = new LangKey("Command.Stats.Usage", "[player]");
    public static final LangKey COMMAND_STATS_DISPLAY = new LangKey("Command.Stats.Display", """
        {message: ~prefix: false;}
        &6&m                  &6&l[ &e&lMy Job Progress &6&l]&6&m                  &6
        &7
        &6        Hover mouse on job name for details!
        &7
        &6▸ {json: ~showText: %job_description%; ~runCommand: /mh info %job_id%;}%job_name%{end-json}    &6Level: &e%job_level%,    &6Exp: &e%job_exp%&7/&e%job_exp_max%
        &6&m                                                        &6""");

    public static final LangKey COMMAND_INFO_USAGE   = new LangKey("Command.Info.Usage", "<job>");
    public static final LangKey COMMAND_INFO_DESC    = new LangKey("Command.Info.Desc", "View info about specified job.");
    public static final LangKey COMMAND_INFO_DISPLAY = new LangKey("Command.Info.Display", """
        {message: ~prefix: false;}
        &6&m                    &6&l[ &e&lJob &7- &e&l%job_name% &6&l]&6&m                    &6
        &7
        &6&lDescription:
        &7%job_description%
        &7
        &a&lProgression:
        &2▸ &aExp: &f%job_exp%&7/&f%job_exp_max%
        &2▸ &aLevel: &f%job_level%&7/&f%job_level_max%
        &2▸ &aMoney Modifier: &fx%job_money_modifier%
        &7
        &d&lBoosters:
        &5▸ &d%booster_type%: &5Exp: &7+%booster_exp_modifier_percent%%&7, &5Money: &7+%booster_money_modifier_percent%%&7, &5Timeleft: &7%booster_time_left%
        &6&m                                                              &6""");

    public static final LangKey COMMAND_BOOSTER_DESC = new LangKey("Command.Booster.Desc", "Manage job boosters.");

    public static final LangKey COMMAND_BOOSTER_PERSONAL_DESC       = new LangKey("Command.Booster.Personal.Desc", "Manage personal player boosters.");
    public static final LangKey COMMAND_BOOSTER_PERSONAL_GIVE_DESC  = new LangKey("Command.Booster.Personal.Give.Desc", "Add/update personal booster for player.");
    public static final LangKey COMMAND_BOOSTER_PERSONAL_GIVE_USAGE = new LangKey("Command.Booster.Personal.Give.Usage", "<player> <name> <jobs> <moneyMod> <expMod> <minutes>");
    public static final LangKey COMMAND_BOOSTER_PERSONAL_GIVE_DONE  = new LangKey("Command.Booster.Personal.Give.Done", "Added personal &fx%booster_money_modifier% Money&7, &fx%booster_exp_modifier% Exp &7booster to &f%player%&7 for &f%booster_time_left%&7. Applicable jobs: &f%booster_jobs%&7.");

    public static final LangKey COMMAND_BOOSTER_PERSONAL_REMOVE_DESC          = new LangKey("Command.Booster.Personal.Remove.Desc", "Remove personal booster from user.");
    public static final LangKey COMMAND_BOOSTER_PERSONAL_REMOVE_USAGE         = new LangKey("Command.Booster.Personal.Remove.Usage", "<player> <booster_id>");
    public static final LangKey COMMAND_BOOSTER_PERSONAL_REMOVE_ERROR_NOTHING = new LangKey("Command.Booster.Personal.Remove.Error.Nothing", "Player &c%player%&7 does not have booster with id &c%booster_id%&7.");
    public static final LangKey COMMAND_BOOSTER_PERSONAL_REMOVE_DONE          = new LangKey("Command.Booster.Personal.Remove.Done", "Removed personal &f%booster_id% &7booster from &f%player%&7.");

    public static final LangKey COMMAND_BOOSTER_GLOBAL_DESC         = new LangKey("Command.Booster.Global.Desc", "Manage global job boosters.");
    public static final LangKey COMMAND_BOOSTER_GLOBAL_CREATE_DESC  = new LangKey("Command.Booster.Global.Create.Desc", "Create/update global job booster.");
    public static final LangKey COMMAND_BOOSTER_GLOBAL_CREATE_USAGE = new LangKey("Command.Booster.Global.Create.Usage", "<name> <jobs> <moneyMod> <expMod> <minutes>");
    public static final LangKey COMMAND_BOOSTER_GLOBAL_CREATE_DONE  = new LangKey("Command.Booster.Global.Create.Done", "Created global &fx%booster_money_modifier% Money&7, &fx%booster_exp_modifier% Exp &7booster for &f%booster_time_left%&7. Applicable jobs: &f%booster_jobs%&7.");
    public static final LangKey COMMAND_BOOSTER_GLOBAL_REMOVE_DESC          = new LangKey("Command.Booster.Global.Remove.Desc", "Remove global job booster.");
    public static final LangKey COMMAND_BOOSTER_GLOBAL_REMOVE_USAGE         = new LangKey("Command.Booster.Global.Remove.Usage", "<booster_id>");
    public static final LangKey COMMAND_BOOSTER_GLOBAL_REMOVE_ERROR_NOTHING = new LangKey("Command.Booster.Global.Remove.Error.Nothing", "No global booster with id &c%booster_id%&7.");
    public static final LangKey COMMAND_BOOSTER_GLOBAL_REMOVE_DONE          = new LangKey("Command.Booster.Global.Remove.Done", "Removed global job &f%booster_id% &7booster.");

    public static final LangKey COMMAND_EXP_DESC      = new LangKey("Command.Exp.Desc", "Manage player's job exp.");
    public static final LangKey COMMAND_EXP_ADD_DESC  = new LangKey("Command.Exp.Add.Desc", "Add exp to player's job.");
    public static final LangKey COMMAND_EXP_ADD_USAGE = new LangKey("Command.Exp.Add.Usage", "<player> <job> <amount> [useBooster]");
    public static final LangKey COMMAND_EXP_ADD_DONE   = new LangKey("Command.Exp.Add.Done", "&7Added &a%amount% &7exp to &a%job_name% &7job for &a%player%&7.");
    public static final LangKey COMMAND_EXP_TAKE_DESC  = new LangKey("Command.Exp.Take.Desc", "Take exp from player's job.");
    public static final LangKey COMMAND_EXP_TAKE_USAGE = new LangKey("Command.Exp.Take.Usage", "<player> <job> <amount>");
    public static final LangKey COMMAND_EXP_TAKE_DONE = new LangKey("Command.Exp.Take.Done", "&7Taken &c%amount% &7exp from &c%job_name% &7job of &c%player%&7.");
    public static final LangKey COMMAND_EXP_SET_DESC  = new LangKey("Command.Exp.Set.Desc", "Set exp of player's job.");
    public static final LangKey COMMAND_EXP_SET_USAGE = new LangKey("Command.Exp.Set.Usage", "<player> <job> <amount>");
    public static final LangKey COMMAND_EXP_SET_DONE  = new LangKey("Command.Exp.Set.Done", "&7Set &e%amount% &7exp to &e%job_name% &7job for &e%player%&7.");

    public static final LangKey COMMAND_LEVEL_DESC      = new LangKey("Command.Level.Desc", "Manage player's job levels.");
    public static final LangKey COMMAND_LEVEL_ADD_DESC  = new LangKey("Command.Level.Add.Desc", "Add level(s) to player's job.");
    public static final LangKey COMMAND_LEVEL_ADD_USAGE = new LangKey("Command.Level.Add.Usage", "<player> <job> <amount>");
    public static final LangKey COMMAND_LEVEL_ADD_DONE   = new LangKey("Command.Level.Add.Done", "&7Added &a%amount% &7level(s) to &a%job_name% &7job for &a%player%&7.");
    public static final LangKey COMMAND_LEVEL_TAKE_DESC  = new LangKey("Command.Level.Take.Desc", "Take level(s) from player's job.");
    public static final LangKey COMMAND_LEVEL_TAKE_USAGE = new LangKey("Command.Level.Take.Usage", "<player> <job> <amount>");
    public static final LangKey COMMAND_LEVEL_TAKE_DONE = new LangKey("Command.Level.Take.Done", "&7Taken &c%amount% &7level(s) from &c%job_name% &7job of &c%player%&7.");
    public static final LangKey COMMAND_LEVEL_SET_DESC  = new LangKey("Command.Level.Set.Desc", "Set level of player's job.");
    public static final LangKey COMMAND_LEVEL_SET_USAGE = new LangKey("Command.Level.Set.Usage", "<player> <job> <amount>");
    public static final LangKey COMMAND_LEVEL_SET_DONE  = new LangKey("Command.Level.Set.Done", "&7Set &e%amount% &7level to &e%job_name% &7job for &e%player%&7.");

    public static final LangKey COMMAND_RESET_DESC  = new LangKey("Command.Reset.Desc", "Reset (player's) job progress.");
    public static final LangKey COMMAND_RESET_USAGE = new LangKey("Command.Reset.Usage", "<jobId> [player]");
    public static final LangKey COMMAND_RESET_DONE  = new LangKey("Command.Reset.Done", "&7Reset &f%job_name% &7job progress for &f%player%&7.");

    public static final LangKey BOOSTER_GLOBAL_NOTIFY   = new LangKey("Booster.Global.Notify", """
        {message: ~prefix: false;}
        &6&m                    &6&l[ &e&lJob Booster &6&l]&6&m                    &6
        &7
        &e    There are the following active job boosters:
        &6        (Hover on booster name for details)
        &7
        &6  ▸ {json: ~showText: &7ID: &f%booster_id%|&7Jobs: &f%booster_jobs%;}&e%booster_type% Booster{end-json} &7: &2Exp: &a+%booster_exp_modifier_percent%%&7, &2Money: &a+%booster_money_modifier_percent%%&7, &2Timeleft: &a%booster_time_left%
        &7
        &6&m                                                                &6""");
    public static final LangKey BOOSTER_PERSONAL_NOTIFY = new LangKey("Booster.Personal.Notify", """
        {message: ~prefix: false;}
        &6&m                    &6&l[ &e&lJob Booster &6&l]&6&m                    &6
        &7
        &a&l       You received %booster_type% Job Booster!
        &7
        &2▸ &aApplicable Jobs: &f%booster_jobs%
        &2▸ &aExp Modifier: &f+%booster_exp_modifier_percent%%
        &2▸ &aMoney Modifier: &f+%booster_money_modifier_percent%%
        &2▸ &aTimeleft: &f%booster_time_left%
        &7
        &6&m                                                                &6""");

    public static final LangKey CURRENCY_ERROR_INVALID = new LangKey("Currency.Error.Invalid", "&cInvalid currency!");

    public static final LangKey JOBS_RESET_SUCCESS     = new LangKey("Jobs.Reset.Success", """
        {message: ~type: TITLES; ~fadeIn: 10; ~stay: 60; ~fadeOut: 20; ~sound: ENTITY_ZOMBIE_BREAK_WOODEN_DOOR;}
        &e&lJob Reset Completed!
        &7All&f %job_name%&7 progress and leveling has been reset!
        """);
    public static final LangKey JOBS_LEVELING_EXP_GAIN = new LangKey("Jobs.Leveling.Exp.Gain", "{message: ~prefix: false; ~type: ACTION_BAR;}&e&l%job_name%: &6+%exp% exp.");
    public static final LangKey JOBS_LEVELING_EXP_LOSE   = new LangKey("Jobs.Leveling.Exp.Lose", "&cYou lost &4%exp% exp &cfrom &4%job_name% &cjob.");
    public static final LangKey JOBS_LEVELING_LEVEL_UP   = new LangKey("Jobs.Leveling.Level.Up", """
        {message: ~prefix: false; ~sound: ENTITY_PLAYER_LEVELUP;}
        &6&m                      &6&l[ &e&lLEVEL UP! &6&l]&6&m                      &6
        &6    Your &e&l%job_name% &6level has been increased to &e&l%job_level%&6!
        &6&m                                                               &6""");
    public static final LangKey JOBS_LEVELING_LEVEL_DOWN     = new LangKey("Jobs.Leveling.Level.Down", """
        {message: ~prefix: false; ~sound: ENTITY_IRON_GOLEM_DEATH;}
        &4&m                        &4&l[ &c&lLEVEL DOWN! &4&l]&4&m                        &4
        &c    Your &4&l%job_name% &clevel has been decreased to &4&l%job_level%&c!
        &c        Be careful with the actions that cost you job exp.
        &4&m                                                                       &4""");
    public static final LangKey JOB_ERROR_INVALID_JOB        = new LangKey("Job.Error.InvalidJob", "&cInvalid job!");
    public static final LangKey JOB_OBJECTIVES_LIMITS_NOTIFY = new LangKey("Job.Objectives.Limits.Notify", """
        {message: ~prefix: false;}
        &4&m                    &4&l[ &c&lJob Daily Limit &4&l]&4&m                    &6
        &7
        &cYou have reached daily &e%limit_type% &climit for &e%objective_name% &cin &e%job_name% &cjob!
        &cYou will not receive &e%limit_type% &cfor &e%objective_name% &cuntil the next day.
        &7
        &4&m                                                                &6""");

    public static final LangKey MONEY_PICKUP = new LangKey("Money.Pickup", "{message: ~type: ACTION_BAR;}&2*** &aYou picked up &f%money%&a! New balance: &f%balance% &2***");
    public static final LangKey MONEY_LOST   = new LangKey("Money.Lost", "{message: ~type: ACTION_BAR;}&4*** &cYou lost &f%money%&c! New balance: &f%balance% &4***");
}
