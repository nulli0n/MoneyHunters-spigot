package su.nightexpress.moneyhunters.pro.config;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.LangMessage;
import su.nexmedia.engine.core.config.CoreLang;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.api.booster.BoosterType;
import su.nightexpress.moneyhunters.pro.api.job.JobState;
import su.nightexpress.moneyhunters.pro.api.money.ObjectiveLimitType;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardType;

public class Lang extends CoreLang {

    public Lang(@NotNull MoneyHunters plugin) {
        super(plugin);
        this.setupEnum(JobState.class);
        this.setupEnum(LeaderboardType.class);
        this.setupEnum(ObjectiveLimitType.class);
        this.setupEnum(BoosterType.class);
    }

    public LangMessage Command_Stats_Desc    = new LangMessage(this, "Show your stats.");
    public LangMessage Command_Stats_Usage   = new LangMessage(this, "[player]");
    public LangMessage Command_Stats_Display = new LangMessage(this, """
        {message: ~prefix: false;}
        &6&m                  &6&l[ &e&lMy Job Progress &6&l]&6&m                  &6
        &7
        &6        Hover mouse on job name for details!
        &7
        &6▸ {json: ~hint: %job_description%; ~chat-type: /mh info %job_id%;}%job_name%{end-json}    &6Level: &e%job_level%,    &6Exp: &e%job_exp%&7/&e%job_exp_max%
        &6&m                                                        &6""");

    public LangMessage Command_Info_Usage   = new LangMessage(this, "<job>");
    public LangMessage Command_Info_Desc    = new LangMessage(this, "View info about specified job.");
    public LangMessage Command_Info_Display = new LangMessage(this, """
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
        &e&lObjectives:
        &6▸ &e{json: ~hint: &7Click to see all objectives!; ~chat-type: /mh objectives %job_id%;}&e&l[Browse]{end-json}
        &7
        &d&lBoosters:
        &5▸ &d%booster_type%: &5Exp: &7+%booster_exp_modifier_percent%%&7, &5Money: &7+%booster_money_modifier_percent%%&7, &5Timeleft: &7%booster_time_left%
        &6&m                                                              &6""");

    public LangMessage Command_Booster_Desc = new LangMessage(this, "Manage job boosters.");

    public LangMessage Command_Booster_Personal_Desc       = new LangMessage(this, "Manage personal player boosters.");
    public LangMessage Command_Booster_Personal_Give_Desc  = new LangMessage(this, "Add/update personal booster for player.");
    public LangMessage Command_Booster_Personal_Give_Usage = new LangMessage(this, "<player> <name> <jobs> <moneyMod> <expMod> <minutes>");
    public LangMessage Command_Booster_Personal_Give_Done  = new LangMessage(this, "Added personal &fx%booster_money_modifier% Money&7, &fx%booster_exp_modifier% Exp &7booster to &f%player%&7 for &f%booster_time_left%&7. Applicable jobs: &f%booster_jobs%&7.");

    public LangMessage Command_Booster_Personal_Remove_Desc          = new LangMessage(this, "Remove personal booster from user.");
    public LangMessage Command_Booster_Personal_Remove_Usage         = new LangMessage(this, "<player> <booster_id>");
    public LangMessage Command_Booster_Personal_Remove_Error_Nothing = new LangMessage(this, "Player &c%player%&7 does not have booster with id &c%booster_id%&7.");
    public LangMessage Command_Booster_Personal_Remove_Done          = new LangMessage(this, "Removed personal &f%booster_id% &7booster from &f%player%&7.");

    public LangMessage Command_Booster_Global_Desc                 = new LangMessage(this, "Manage global job boosters.");
    public LangMessage Command_Booster_Global_Create_Desc          = new LangMessage(this, "Create/update global job booster.");
    public LangMessage Command_Booster_Global_Create_Usage         = new LangMessage(this, "<name> <jobs> <moneyMod> <expMod> <minutes>");
    public LangMessage Command_Booster_Global_Create_Done          = new LangMessage(this, "Created global &fx%booster_money_modifier% Money&7, &fx%booster_exp_modifier% Exp &7booster for &f%booster_time_left%&7. Applicable jobs: &f%booster_jobs%&7.");
    public LangMessage Command_Booster_Global_Remove_Desc          = new LangMessage(this, "Remove global job booster.");
    public LangMessage Command_Booster_Global_Remove_Usage         = new LangMessage(this, "<booster_id>");
    public LangMessage Command_Booster_Global_Remove_Error_Nothing = new LangMessage(this, "No global booster with id &c%booster_id%&7.");
    public LangMessage Command_Booster_Global_Remove_Done          = new LangMessage(this, "Removed global job &f%booster_id% &7booster.");

    public LangMessage Command_Exp_Desc       = new LangMessage(this, "Manage player's job exp.");
    public LangMessage Command_Exp_Add_Desc   = new LangMessage(this, "Add exp to player's job.");
    public LangMessage Command_Exp_Add_Usage  = new LangMessage(this, "<player> <job> <amount> [useBooster]");
    public LangMessage Command_Exp_Add_Done   = new LangMessage(this, "&7Added &a%amount% &7exp to &a%job_name% &7job for &a%player%&7.");
    public LangMessage Command_Exp_Take_Desc  = new LangMessage(this, "Take exp from player's job.");
    public LangMessage Command_Exp_Take_Usage = new LangMessage(this, "<player> <job> <amount>");
    public LangMessage Command_Exp_Take_Done  = new LangMessage(this, "&7Taken &c%amount% &7exp from &c%job_name% &7job of &c%player%&7.");
    public LangMessage Command_Exp_Set_Desc   = new LangMessage(this, "Set exp of player's job.");
    public LangMessage Command_Exp_Set_Usage  = new LangMessage(this, "<player> <job> <amount>");
    public LangMessage Command_Exp_Set_Done   = new LangMessage(this, "&7Set &e%amount% &7exp to &e%job_name% &7job for &e%player%&7.");

    public LangMessage Command_Level_Desc       = new LangMessage(this, "Manage player's job levels.");
    public LangMessage Command_Level_Add_Desc   = new LangMessage(this, "Add level(s) to player's job.");
    public LangMessage Command_Level_Add_Usage  = new LangMessage(this, "<player> <job> <amount>");
    public LangMessage Command_Level_Add_Done   = new LangMessage(this, "&7Added &a%amount% &7level(s) to &a%job_name% &7job for &a%player%&7.");
    public LangMessage Command_Level_Take_Desc  = new LangMessage(this, "Take level(s) from player's job.");
    public LangMessage Command_Level_Take_Usage = new LangMessage(this, "<player> <job> <amount>");
    public LangMessage Command_Level_Take_Done  = new LangMessage(this, "&7Taken &c%amount% &7level(s) from &c%job_name% &7job of &c%player%&7.");
    public LangMessage Command_Level_Set_Desc   = new LangMessage(this, "Set level of player's job.");
    public LangMessage Command_Level_Set_Usage  = new LangMessage(this, "<player> <job> <amount>");
    public LangMessage Command_Level_Set_Done   = new LangMessage(this, "&7Set &e%amount% &7level to &e%job_name% &7job for &e%player%&7.");

    public LangMessage Command_Reset_Desc  = new LangMessage(this, "Reset (player's) job progress.");
    public LangMessage Command_Reset_Usage = new LangMessage(this, "<jobId> [player]");
    public LangMessage Command_Reset_Done  = new LangMessage(this, "&7Reset &f%job_name% &7job progress for &f%player%&7.");

    public LangMessage Command_Jobs_Desc = new LangMessage(this, "Browse all jobs.");

    public LangMessage Command_Objectives_Usage = new LangMessage(this, "<job>");
    public LangMessage Command_Objectives_Desc  = new LangMessage(this, "View job objectives.");

    public LangMessage Command_Drop_Usage = new LangMessage(this, "<currency> (<amount> or <min>:<max>) <world> <x> <y> <z>");
    public LangMessage Command_Drop_Desc  = new LangMessage(this, "Create and drop money item.");
    public LangMessage Command_Drop_Done  = new LangMessage(this, "Dropped &a%money% &7money item at &f%x%&7, &f%y%&7, &f%z%&7 in world &f%world%&7.");

    public LangMessage Command_Leaderboard_Desc                 = new LangMessage(this, "View or manage leaderboards.");
    public LangMessage Command_Leaderboard_Hologram_Desc        = new LangMessage(this, "Manage holographic leaderboards.");
    public LangMessage Command_Leaderboard_Hologram_Add_Usage   = new LangMessage(this, "<boardType> <jobId>");
    public LangMessage Command_Leaderboard_Hologram_Add_Desc    = new LangMessage(this, "Create holographic leaderboard.");
    public LangMessage Command_Leaderboard_Hologram_Add_Done    = new LangMessage(this, "Added holographic Leaderboard!");
    public LangMessage Command_Leaderboard_Hologram_Remove_Desc = new LangMessage(this, "Remove nearest holographic leaderboard.");
    public LangMessage Command_Leaderboard_Hologram_Remove_Done = new LangMessage(this, "Removed nearest (if there was any) holograhpic leaderboard.");
    public LangMessage Command_Leaderboard_List_Usage           = new LangMessage(this, "<boardType> <jobId>");
    public LangMessage Command_Leaderboard_List_Desc            = new LangMessage(this, "View leaderboard for certain job.");
    public LangMessage Command_Leaderboard_List_Format          = new LangMessage(this, """
        {message: ~prefix: false;}
        &6&m                  &6&l[ &e&l%job% &7- &e&l%type% &6&l]&6&m                  &6
        &7
        &6        Click on player name to view user stats!
        &7
        &6#%position% {json: ~showText: &bClick to view player stats!; ~runCommand: /mh stats %name%;}&e%name%{end-json}    &6Score: &e%score%
        &6&m                                                             &6""");

    public LangMessage Booster_Global_Notify   = new LangMessage(this, """
        {message: ~prefix: false;}
        &6&m                    &6&l[ &e&lJob Booster &6&l]&6&m                    &6
        &7
        &e    There are the following active job boosters:
        &6        (Hover on booster name for details)
        &7
        &6  ▸ {json: ~hint: &7ID: &f%booster_id%|&7Jobs: &f%booster_jobs%;}&e%booster_type% Booster{end-json} &7: &2Exp: &a+%booster_exp_modifier_percent%%&7, &2Money: &a+%booster_money_modifier_percent%%&7, &2Timeleft: &a%booster_time_left%
        &7
        &6&m                                                                &6""");
    public LangMessage Booster_Personal_Notify = new LangMessage(this, """
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

    public LangMessage Currency_Error_Invalid = new LangMessage(this, "&cInvalid currency!");

    public LangMessage Jobs_State_Change_Error_Nothing = new LangMessage(this, "{message: ~sound: ENTITY_VILLAGER_NO;}&cState of this job can not be changed!");
    public LangMessage Jobs_State_Change_Error_Limit   = new LangMessage(this, """
        {message: ~type: TITLES; ~fadeIn: 10; ~stay: 60; ~fadeOut: 20; ~sound: BLOCK_ANVIL_PLACE;}
        &c&lState Not Changed!
        &7You already have &c%jobs_have%&7/&c%jobs_limit% &f%job_state%&7 jobs!
        """);
    public LangMessage Jobs_State_Change_Error_Level   = new LangMessage(this, """
        {message: ~type: TITLES; ~fadeIn: 10; ~stay: 60; ~fadeOut: 20; ~sound: BLOCK_ANVIL_PLACE;}
        &c&lState Not Changed!
        &f%job_level%&7 level is exceed the max. level of its &f%job_state%&7 state!
        """);
    public LangMessage Jobs_State_Change_Success       = new LangMessage(this, """
        {message: ~type: TITLES; ~fadeIn: 10; ~stay: 60; ~fadeOut: 20; ~sound: UI_BUTTON_CLICK;}
        &a&lState Changed!
        &f%job_name%&7 is &f%job_state%&7 now!
        """);
    public LangMessage Jobs_Reset_Success              = new LangMessage(this, """
        {message: ~type: TITLES; ~fadeIn: 10; ~stay: 60; ~fadeOut: 20; ~sound: ENTITY_ZOMBIE_BREAK_WOODEN_DOOR;}
        &e&lJob Reset Completed!
        &7All&f %job_name%&7 progress and leveling has been reset!
        """);
    public LangMessage Jobs_Leveling_Exp_Gain          = new LangMessage(this, "{message: ~prefix: false; ~type: ACTION_BAR;}&e&l%job_name%: &6+%exp% exp.");
    public LangMessage Jobs_Leveling_Exp_Lose          = new LangMessage(this, "&cYou lost &4%exp% exp &cfrom &4%job_name% &cjob.");
    public LangMessage Jobs_Leveling_Level_Up          = new LangMessage(this, """
        {message: ~prefix: false; ~sound: ENTITY_PLAYER_LEVELUP;}
        &6&m                      &6&l[ &e&lLEVEL UP! &6&l]&6&m                      &6
        &6    Your &e&l%job_name% &6level has been increased to &e&l%job_level%&6!
        &6&m                                                               &6""");
    public LangMessage Jobs_Leveling_Level_Down        = new LangMessage(this, """
        {message: ~prefix: false; ~sound: ENTITY_IRON_GOLEM_DEATH;}
        &4&m                        &4&l[ &c&lLEVEL DOWN! &4&l]&4&m                        &4
        &c    Your &4&l%job_name% &clevel has been decreased to &4&l%job_level%&c!
        &c        Be careful with the actions that cost you job exp.
        &4&m                                                                       &4""");
    public LangMessage Job_Error_InvalidJob            = new LangMessage(this, "&cInvalid job!");
    public LangMessage Job_Objectives_Limits_Notify    = new LangMessage(this, """
        {message: ~prefix: false;}
        &4&m                    &4&l[ &c&lJob Daily Limit &4&l]&4&m                    &6
        &7
        &cYou have reached daily &e%limit_type% &climit for &e%objective_name% &cin &e%job_name% &cjob!
        &cYou will not receive &e%limit_type% &cfor &e%objective_name% &cuntil the next day.
        &7
        &4&m                                                                &6""");

    public LangMessage Money_Pickup = new LangMessage(this, "{message: ~type: ACTION_BAR;}&2*** &aYou picked up &f%money%&a! New balance: &f%balance% &2***");
    public LangMessage Money_Lost   = new LangMessage(this, "{message: ~type: ACTION_BAR;}&4*** &cYou lost &f%money%&c! New balance: &f%balance% &4***");
}
