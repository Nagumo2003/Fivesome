package models.strategies;
import models.Character;

public interface SkillStrategy {
    String execute(Character user, Character target) throws Exception;
    String getName();
}