package com.uni4team.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.uni4team.sprites.*;
import javafx.util.Pair;

import java.util.*;


public class PlayState extends States {
    private Texture bg;
    private Label scoreLbl;
    private int backgroundWidth, backgroundHeight, gardenWidth, gardenHeight, posx, posy, plantSun, Score, drawSun;
    private final int timer;
    private Rectangle firstBottomRectangle;
    private ShapeRenderer shape;
    private PeaShooter peaShooter;
    private sunFlower sunFlower;
    private Map<Pair<Integer, Integer>, Boolean> positions;
    private boolean selectPeashooter, selectSunFlower;
    private List<PeaShooter> peaOnScreen;
    private List<sunFlower> sunFlowersOnScreen;
    private List<singlePea> singlePeas;
    private List<Sun> sunsRandom;
    //------------------------------------------------------
    private StandardZombie standardZombie;
    private Array<Lawnmowers> lawnmowers;

    Random rand = new Random();

    public PlayState(GameStateManager gsm) {
        super(gsm);
        bg = new Texture("backyardEdited.jpg");
        backgroundWidth = bg.getWidth();
        backgroundHeight = bg.getHeight();
        gardenWidth = 81;
        gardenHeight = 150;
        selectPeashooter = false;
        selectSunFlower = false;
        peaOnScreen = new ArrayList<>();
        sunFlowersOnScreen = new ArrayList<>();
        firstBottomRectangle = new Rectangle();
        shape = new ShapeRenderer();
        firstBottomRectangle.setHeight(gardenHeight);
        firstBottomRectangle.setWidth(gardenWidth);
        firstBottomRectangle.setPosition(250, 35);
        peaShooter = new PeaShooter(2, 250);
        sunFlower = new sunFlower(2, 400);
        positions = new HashMap<>();
        singlePeas = new ArrayList<singlePea>();
        plantSun = 5;
        drawSun = 0;
        Score = 50;
        timer = 100;
        sunsRandom = new ArrayList<>();
        Zombies.arrayOfZombies = new Array<Zombies>();
        lawnmowers = new Array<Lawnmowers>();

        for (int i = 0; i < 5; i++) {
            lawnmowers.add(new Lawnmowers(170, Zombies.main5RowPositions[i]));
        }

        for (int i = 0; i < 5; i++) {
            int last = 600;
            for (int j = 0; j < 10 + rand.nextInt(10); j++) {
                last = last + Zombies.distanceBetweenZombies + rand.nextInt(200);
                int choosen = rand.nextInt(2);
                if (choosen == 0)
                    Zombies.arrayOfZombies.add(new StandardZombie(1200, last, Zombies.main5RowPositions[i], 0.2f));
                else Zombies.arrayOfZombies.add(new bucketHeadZombie(1200, last, Zombies.main5RowPositions[i], 0.2f));
            }
        }

        for (int w = (int) (firstBottomRectangle.getX()); w < backgroundWidth - 50; w += firstBottomRectangle.width)
            for (int h = (int) (firstBottomRectangle.getY()); h < backgroundHeight; h += firstBottomRectangle.height)
                positions.put(new Pair(w, h), true);
    }

    @Override
    protected void handleInput() {
    }

    @Override
    public void update(float dt) {
        // collision of zombies with peas
        for (Zombies zombie : Zombies.arrayOfZombies) {
            for (singlePea pea : singlePeas) {
                if (zombie.getZombieState() == 3) continue;
                if (zombie.getPosition().y + 64 == pea.getPosition().getValue() &&
                        zombie.getPosition().x + 25 < pea.getPosition().getKey() && pea.getPosition().getKey() < zombie.getPosition().x + 30) {

                    boolean isDead = zombie.hit(singlePea.getHitCost());
                    pea.setPosition(pea.getPosition().getKey(), 5000);
                    if (isDead) {
                        zombie.setSpeed(0);
                        zombie.setZombieState(3);
                        Zombies.deadCnt++;

                        // when zombie dies decrease the distance between zombies
                        // and adding another one int the end if the size of the array mod 5 == 0
                        Zombies.distanceBetweenZombies -= 50;
                        if (Zombies.distanceBetweenZombies < 10) Zombies.distanceBetweenZombies = 10;
                        if (Zombies.deadCnt % 5 == 0) {
                            Zombies.arrayOfZombies.add(new StandardZombie(1200, 2000 + rand.nextInt(5000), Zombies.main5RowPositions[rand.nextInt(5)], 0.2f));
                        }
                        // the only thing left is respawn the died zombie on a different coordinate
                    }
                }
            }
        }

        for (Zombies zombie : Zombies.arrayOfZombies) {
            // collision of zombies with sun flower
            for (sunFlower flower : sunFlowersOnScreen) {
                if (zombie.getZombieState() != 3 && zombie.getPosition().y + 5 == flower.getPosition().getValue() &&
                        zombie.getPosition().x + 10 < flower.getPosition().getKey() && flower.getPosition().getKey() < zombie.getPosition().x + 20) {

                    zombie.setSpeed(0);
                    if (zombie.getZombieState() != 2) zombie.setZombieState(2);
                    boolean isDead = flower.hit(zombie.getHitCost());
                    if (isDead) {
                        positions.remove(flower.getPosition());
                        positions.put(flower.getPosition(), true);
                        flower.setPosition(5000, 5000);
                        if(flower.getHasSun() == true){
                            flower.setHasSun(false);
                            flower.getSun().dispose();
                        }
                        flower.dispose();
                        zombie.setZombieState(1);
                        zombie.setSpeed(0.2f);
                    }
                }
            }

            // collision of zombies with pea shooter
            for (PeaShooter shooter : peaOnScreen) {
                if (zombie.getZombieState() != 3 && zombie.getPosition().y + 5 == shooter.getPosition().getValue() &&
                        zombie.getPosition().x + 10 < shooter.getPosition().getKey() && shooter.getPosition().getKey() < zombie.getPosition().x + 20) {

                    zombie.setSpeed(0);
                    if (zombie.getZombieState() != 2) zombie.setZombieState(2);
                    boolean isDead = shooter.hit(zombie.getHitCost());
                    if (isDead) {
                        positions.remove(shooter.getPosition());
                        positions.put(shooter.getPosition(), true);
                        shooter.setPosition(5000, 5000);
                        shooter.dispose();
                        zombie.setZombieState(1);
                        zombie.setSpeed(0.2f);
                    }
                }
            }
        }

        // collision of zombie with lawnmowers
        for (Lawnmowers lawnmower : lawnmowers) {
            for (Zombies zombie : Zombies.arrayOfZombies) {
                if (lawnmower.getPosition().y == zombie.getPosition().y &&
                        lawnmower.getPosition().x < zombie.getPosition().x && zombie.getPosition().x < lawnmower.getPosition().x + 10) {
                    lawnmower.setSpeed(5);
                    zombie.setSpeed(0f);
                    if (zombie.getZombieState() != 3)
                        zombie.setZombieState(3);
                }
            }
        }

        for (Zombies zombie : Zombies.arrayOfZombies)
            zombie.update(dt, gsm);

        for (singlePea pea : singlePeas)
            pea.update(dt, gsm);

        for(Sun suns : sunsRandom)
            suns.update(dt, gsm);

        for (Zombies zombie : Zombies.arrayOfZombies) {
            if (zombie.getZombieState() == 3 && zombie.getZombieHeadAnimation().isTaken()) {
                zombie.setZombieState(1);
                zombie.setHpPoint(1200);
                zombie.setSpeed(0.2f);
                zombie.setPosition(2000 + rand.nextInt(5000), (int) zombie.getPosition().y);
            }
        }

        for (Lawnmowers lawnmower : lawnmowers) {
            lawnmower.update(dt);
        }
    }

    public void drawElements(SpriteBatch sb){
        sb.begin();
        sb.draw(bg, 0, 0);

        peaShooter.render(sb);
        sunFlower.render(sb);

        for (Lawnmowers lawnmower : lawnmowers)
            lawnmower.render(sb);

        for (int i = 0; i < peaOnScreen.size(); i++)
            peaOnScreen.get(i).render(sb);

        for (int i = 0; i < sunFlowersOnScreen.size(); i++) {
            sunFlowersOnScreen.get(i).render(sb);
            if (sunFlowersOnScreen.get(i).getHasSun() == true)
                sunFlowersOnScreen.get(i).getSun().render(sb);
            else if (plantSun % timer == 0) {
                sunFlowersOnScreen.get(i).setSun(new Sun(sunFlowersOnScreen.get(i).getPosition().getKey(), sunFlowersOnScreen.get(i).getPosition().getValue(), false));
                sunFlowersOnScreen.get(i).setHasSun(true);
            }
        }

        for (int i = 0; i < singlePeas.size(); i++)
            sb.draw(singlePeas.get(i).getTexture(), singlePeas.get(i).getPosition().getKey(), singlePeas.get(i).getPosition().getValue());

        for (Zombies zombie : Zombies.arrayOfZombies)
            zombie.render(sb);

        for (int i = 0; i < sunsRandom.size(); i++)
            sunsRandom.get(i).render(sb);

        sb.end();
    }

    public void drawText(){
        SpriteBatch spriteBatch;
        BitmapFont textOfScore, textOfCostOfPeaShooter, textOfCostOfSunFlower;
        CharSequence str = String.valueOf(Score);
        spriteBatch = new SpriteBatch();
        textOfScore = new BitmapFont();
        textOfCostOfPeaShooter = new BitmapFont();
        textOfCostOfSunFlower = new BitmapFont();
        spriteBatch.begin();

        textOfScore.setColor(Color.BROWN);
        textOfScore.getData().setScale(2, 2);
        textOfScore.draw(spriteBatch, str, 28, 113);

        textOfCostOfPeaShooter.setColor(Color.WHITE);
        textOfCostOfPeaShooter.getData().setScale((float)(1.2), (float)(1.2));
        textOfCostOfPeaShooter.draw(spriteBatch, "Cost: 25", 12, 245);

        textOfCostOfSunFlower.setColor(Color.WHITE);
        textOfCostOfSunFlower.getData().setScale((float)(1.2), (float)(1.2));
        textOfCostOfSunFlower.draw(spriteBatch, "Cost: 50", 12, 395);
        spriteBatch.end();
    }

    @Override
    public void render(SpriteBatch sb) {
        plantSun++;
        drawSun++;

        drawElements(sb);

        drawText();

        if(drawSun % timer == 0)
            sunsRandom.add(new Sun(rand.nextInt(gardenWidth * 8) + 250, 700, true));

        if (Gdx.input.justTouched()) {
            posx = Gdx.input.getX();
            posy = Gdx.graphics.getHeight() - Gdx.input.getY();
            if (Score >= peaShooter.getCostOfPlant() && posx >= peaShooter.getPosition().getKey() && posx <= peaShooter.getPosition().getKey() + peaShooter.getPicOfPlant().getWidth() && posy >= peaShooter.getPosition().getValue() && posy <= peaShooter.getPicOfPlant().getHeight() + peaShooter.getPosition().getValue()) {
                selectSunFlower = false;
                selectPeashooter = true;
            } else if (selectPeashooter) {
                selectPeashooter = false;
                Set<Pair<Integer, Integer>> st = positions.keySet();
                for (Pair<Integer, Integer> pr : st) {
                    if (positions.get(pr) == true && posx >= pr.getKey() && posx <= pr.getKey() + gardenWidth && posy >= pr.getValue() && posy <= gardenHeight + pr.getValue()) {
                        Score -= peaShooter.getCostOfPlant();
                        PeaShooter pea = new PeaShooter(pr.getKey(), pr.getValue());
                        peaOnScreen.add(pea);
                        singlePeas.add(new singlePea(pea.getPosition().getKey() + ((pea.getPicOfPlant().getWidth() + 20) / 2), pea.getPosition().getValue() + ((pea.getPicOfPlant().getHeight() + 20) / 2), 4f, pea));
                        positions.remove(pr);
                        positions.put(pr, false);
                        break;
                    }
                }
            } else if (Score >= sunFlower.getCostOfPlant() && posx >= sunFlower.getPosition().getKey() && posx <= sunFlower.getPosition().getKey() + sunFlower.getPicOfPlant().getWidth() && posy >= sunFlower.getPosition().getValue() && posy <= sunFlower.getPicOfPlant().getHeight() + sunFlower.getPosition().getValue()) {
                selectPeashooter = false;
                selectSunFlower = true;
            } else if (selectSunFlower) {
                selectSunFlower = false;
                Set<Pair<Integer, Integer>> st = positions.keySet();
                for (Pair<Integer, Integer> pr : st) {
                    if (positions.get(pr) == true && posx >= pr.getKey() && posx <= pr.getKey() + gardenWidth && posy >= pr.getValue() && posy <= gardenHeight + pr.getValue()) {
                        Score -= sunFlower.getCostOfPlant();
                        sunFlower sunflowerTemp = new sunFlower(pr.getKey(), pr.getValue());
                        sunFlowersOnScreen.add(sunflowerTemp);
                        positions.remove(pr);
                        positions.put(pr, false);
                        break;
                    }
                }
            } else {
                Boolean clickedOnSunOfSunFlower = false;
                for (int k = 0; k < sunFlowersOnScreen.size(); k++) {
                    if (sunFlowersOnScreen.get(k).getHasSun() == true) {
                        if (posx >= sunFlowersOnScreen.get(k).getPosition().getKey() && posx <= sunFlowersOnScreen.get(k).getPosition().getKey() + 30 &&
                                posy >= sunFlowersOnScreen.get(k).getPosition().getValue() && posy <= sunFlowersOnScreen.get(k).getPosition().getValue() + 30) {
                            Score += 25;
                            sunFlowersOnScreen.get(k).setHasSun(false);
                            sunFlowersOnScreen.get(k).getSun().dispose();
                            clickedOnSunOfSunFlower = true;
                            break;
                        }
                    }
                }
                if(clickedOnSunOfSunFlower == false){
                    for(int i = 0; i < sunsRandom.size(); i++){
                        if(posx >= sunsRandom.get(i).getPosition().getKey() && posx <= sunsRandom.get(i).getPosition().getKey() + 100 &&
                           posy >= sunsRandom.get(i).getPosition().getValue() && posy <= sunsRandom.get(i).getPosition().getValue() + 100){
                            Score += 25;
                            sunsRandom.get(i).setPosition(5000, 5000);
                            sunsRandom.get(i).dispose();
                        }
                    }
                }
            }
        }

        if (selectPeashooter) {
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.rect(peaShooter.getPosition().getKey(), peaShooter.getPosition().getValue(), peaShooter.getPicOfPlant().getWidth(), peaShooter.getPicOfPlant().getHeight());
            shape.setColor(Color.BLUE);
            shape.end();
        }
        if (selectSunFlower) {
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.rect(sunFlower.getPosition().getKey(), sunFlower.getPosition().getValue(), sunFlower.getPicOfPlant().getWidth() + 1, sunFlower.getPicOfPlant().getHeight());
            shape.setColor(Color.BLUE);
            shape.end();
        }
    }

    @Override
    public void dispose() {
        shape.dispose();
        peaShooter.dispose();
        sunFlower.dispose();
        bg.dispose();
    }
}