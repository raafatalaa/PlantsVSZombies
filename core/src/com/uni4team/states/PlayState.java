package com.uni4team.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.uni4team.sprites.*;
import javafx.util.Pair;

import java.util.*;


public class PlayState extends States {
    private Texture bg;
    private Label scoreLbl;
    private int backgroundWidth, backgroundHeight, gardenWidth, gardenHeight, posx, posy, plantSun, Score;
    private Rectangle firstBottomRectangle;
    private ShapeRenderer shape;
    private PeaShooter peaShooter;
    private sunFlower sunFlower;
    private Map<Pair<Integer, Integer>, Boolean> positions;
    private boolean selectPeashooter, selectSunFlower;
    private List<PeaShooter> peaOnScreen;
    private List<sunFlower> sunFlowersOnScreen;
    private List<singlePea> singlePeas;
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
        plantSun = 0;
        Score = 50;
        Zombies.arrayOfZombies = new Array<Zombies>();
        lawnmowers = new Array<Lawnmowers>();

        for (int i = 0; i < 5; i++) {
            lawnmowers.add(new Lawnmowers(170, Zombies.main5RowPositions[i]));
        }

        for (int i = 0; i < 5; i++) {
            int last = 600;
            for (int j = 0; j < 10 + rand.nextInt(10); j++) {
                last = last + Zombies.distanceBetweenZombies + rand.nextInt(200);
                Zombies.arrayOfZombies.add(new StandardZombie(1200, last, Zombies.main5RowPositions[i], 0.2f));
            }
        }

        for (int w = (int) (firstBottomRectangle.getX()); w < backgroundWidth - 5; w += firstBottomRectangle.width)
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
                if(zombie.getZombieState()==3)continue;
               if(zombie.getPosition().y + 64 == pea.getPosition().getValue() &&
                       zombie.getPosition().x + 25 < pea.getPosition().getKey() && pea.getPosition().getKey() < zombie.getPosition().x + 30){

                   boolean isDead=zombie.hit(singlePea.getHitCost());
                   pea.setPosition(pea.getPosition().getKey(),5000);
                   if(isDead) {
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
                if(zombie.getZombieState()!=3&&zombie.getPosition().y + 5 == flower.getPosition().getValue() &&
                        zombie.getPosition().x + 10 < flower.getPosition().getKey() && flower.getPosition().getKey() < zombie.getPosition().x + 20){

                    zombie.setSpeed(0);
                    if(zombie.getZombieState()!=2)zombie.setZombieState(2);
                    boolean isDead=flower.hit(zombie.getHitCost());
                    if(isDead){
                        flower.setPosition(5000,5000);
                        flower.dispose();
                        zombie.setZombieState(1);
                        zombie.setSpeed(0.2f);
                    }
                }
            }
            // collision of zombies with pea shooter
            for (PeaShooter shooter : peaOnScreen) {
                if(zombie.getZombieState()!=3&&zombie.getPosition().y + 5 == shooter.getPosition().getValue() &&
                        zombie.getPosition().x + 10 < shooter.getPosition().getKey() && shooter.getPosition().getKey() < zombie.getPosition().x + 20){

                    zombie.setSpeed(0);
                    if(zombie.getZombieState()!=2)zombie.setZombieState(2);
                    boolean isDead=shooter.hit(zombie.getHitCost());
                    if(isDead){
                        shooter.setPosition(5000,5000);
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
                    //zombie.dispose();
                }
            }
        }
        for (Zombies zombie : Zombies.arrayOfZombies)
            zombie.update(dt, gsm);

        for (singlePea pea : singlePeas)
            pea.update(dt, gsm);
        for(Zombies zombie : Zombies.arrayOfZombies) {
            if (zombie.getZombieState()==3 && zombie.getZombieHeadAnimation().isTaken()) {
                    zombie.setZombieState(1);
                    zombie.setHpPoint(1200);
                    zombie.setSpeed(0.2f);
                    zombie.setPosition(2000+rand.nextInt(5000),(int)zombie.getPosition().y);
            }
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        plantSun++;
        sb.begin();
        sb.draw(bg, 0, 0);

        peaShooter.render(sb);
        sunFlower.render(sb);

        for (Lawnmowers lawnmower : lawnmowers) {
            lawnmower.render(sb);
        }

        for (int i = 0; i < peaOnScreen.size(); i++)
            peaOnScreen.get(i).render(sb);

        for(int i = 0; i < sunFlowersOnScreen.size(); i++){
            sunFlowersOnScreen.get(i).render(sb);
            if(sunFlowersOnScreen.get(i).getHasSun() == true)
                sunFlowersOnScreen.get(i).getSun().render(sb, sunFlowersOnScreen.get(i).getPosition().getKey(), sunFlowersOnScreen.get(i).getPosition().getValue());
            else if(plantSun % 500 == 0){
                sunFlowersOnScreen.get(i).setSun(new Sun(sunFlowersOnScreen.get(i).getPosition().getKey(), sunFlowersOnScreen.get(i).getPosition().getValue()));
                sunFlowersOnScreen.get(i).setHasSun(true);
            }
        }

        for (int i = 0 ; i < singlePeas.size() ; i++)
            sb.draw(singlePeas.get(i).getTexture(),singlePeas.get(i).getPosition().getKey(), singlePeas.get(i).getPosition().getValue());
        for (Zombies zombie : Zombies.arrayOfZombies) {
            zombie.render(sb);
        }
        sb.end();

        SpriteBatch spriteBatch;
        BitmapFont font;
        CharSequence str = String.valueOf(Score);
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        spriteBatch.begin();
        font.setColor(Color.BROWN);
        font.getData().setScale(2, 2);
        font.draw(spriteBatch, str, 28, 113);
        spriteBatch.end();


        if (Gdx.input.justTouched()) {
            posx = Gdx.input.getX();
            posy = Gdx.graphics.getHeight() - Gdx.input.getY();
            if (Score >= peaShooter.getCostOfPeaShooter() && posx >= peaShooter.getPosition().getKey() && posx <= peaShooter.getPosition().getKey() + peaShooter.getPeaShooterGIF().getWidth() && posy >= peaShooter.getPosition().getValue() && posy <= peaShooter.getPeaShooterGIF().getHeight() + peaShooter.getPosition().getValue()) {
                selectSunFlower = false;
                selectPeashooter = true;
            }
            else if (selectPeashooter) {
                selectPeashooter = false;
                Set<Pair<Integer, Integer>> st = positions.keySet();
                for (Pair<Integer, Integer> pr : st) {
                    if (positions.get(pr) == true && posx >= pr.getKey() && posx <= pr.getKey() + gardenWidth && posy >= pr.getValue() && posy <= gardenHeight + pr.getValue()) {
                        Score -= peaShooter.getCostOfPeaShooter();
                        PeaShooter pea = new PeaShooter(pr.getKey(), pr.getValue());
                        peaOnScreen.add(pea);
                        singlePeas.add(new singlePea(pea.getPosition().getKey() + ((pea.getPeaShooterGIF().getWidth() + 20)/2), pea.getPosition().getValue() + ((pea.getPeaShooterGIF().getHeight() + 20)/2),4f, pea));
                        positions.remove(pr);
                        positions.put(pr, false);
                        break;
                    }
                }
            }
            else if (Score >= sunFlower.getCostOfSunFlower() && posx >= sunFlower.getPosition().getKey() && posx <= sunFlower.getPosition().getKey() + sunFlower.getSunFlowerGIF().getWidth() && posy >= sunFlower.getPosition().getValue() && posy <= sunFlower.getSunFlowerGIF().getHeight() + sunFlower.getPosition().getValue()) {
                selectPeashooter = false;
                selectSunFlower = true;
            }
            else if (selectSunFlower) {
                selectSunFlower = false;
                Set<Pair<Integer, Integer>> st = positions.keySet();
                for (Pair<Integer, Integer> pr : st) {
                    if (positions.get(pr) == true && posx >= pr.getKey() && posx <= pr.getKey() + gardenWidth && posy >= pr.getValue() && posy <= gardenHeight + pr.getValue()) {
                        Score -= sunFlower.getCostOfSunFlower();
                        sunFlower sunflowerTemp = new sunFlower(pr.getKey(), pr.getValue());
                        sunFlowersOnScreen.add(sunflowerTemp);
                        positions.remove(pr);
                        positions.put(pr, false);
                        break;
                    }
                }
            }
            else {
                for(int k = 0; k < sunFlowersOnScreen.size(); k++){
                    if(sunFlowersOnScreen.get(k).getHasSun() == true){
                        if(posx >= sunFlowersOnScreen.get(k).getPosition().getKey() &&  posx <= sunFlowersOnScreen.get(k).getPosition().getKey() + 30 &&
                        posy >= sunFlowersOnScreen.get(k).getPosition().getValue() && posy <= sunFlowersOnScreen.get(k).getPosition().getValue() + 30){
                            Score += 25;
                            sunFlowersOnScreen.get(k).setHasSun(false);
                            sunFlowersOnScreen.get(k).getSun().dispose();
                            break;
                        }
                    }
                }
            }
        }

        if (selectPeashooter) {
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.rect(peaShooter.getPosition().getKey(), peaShooter.getPosition().getValue(), peaShooter.getPeaShooterGIF().getWidth(),peaShooter.getPeaShooterGIF().getHeight());
            shape.setColor(Color.BLUE);
            shape.end();
        }
        if(selectSunFlower){
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.rect(sunFlower.getPosition().getKey(), sunFlower.getPosition().getValue(), sunFlower.getSunFlowerGIF().getWidth() + 1, sunFlower.getSunFlowerGIF().getHeight());
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