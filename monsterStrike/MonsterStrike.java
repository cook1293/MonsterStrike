package monsterStrike;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * モンスターストライク風ゲームアプリケーション
 * @author proglight
 */
public class MonsterStrike extends JFrame{
	MsPanel pn = new MsPanel();		//描画パネルのインスタンス
	Random rand = new Random();		//乱数

	static final int SIZEP = 60;					//プレーヤーサイズ
	static final int SIZEE = 60;					//敵サイズ
	static final int SIZEH = 40;					//ハートサイズ
	static final int SIZEWINX = 400,SIZEWINY = 510;	//ウィンドウサイズ
	static final int ERRORWINY = 30;				//ウィンドウサイズの誤差
	static final int PLAYERS = 4;					//プレーヤー数
	static final int ENEMIES = 3;					//敵数
	static final int SIZEFONT = 16;					//フォントサイズ

	double px,py;			//クリック時の座標
	double fx,fy;			//クリックを放した時の座標
	double rx,ry,rl;		//距離計算用
	double sx,sy;			//実際の速度
	double mx,my;			//減速計算用
	double rate;			//x速度とy速度の比率

	int flgTouch = 0;					//クリックされたかのフラグ
	int flgPAtt[] = {0,0,0};							//各敵とプレーヤーの衝突フラグ(要素：敵)
	int flgEAtt[][] = {{0,0,0,0},{0,0,0,0},{0,0,0,0}};		//各敵の攻撃と各プレーヤーの衝突フラグ(行：敵、列：プレーヤー)
	int flgFtoP[] = {0,0,0,0};						//各プレーヤー同士の衝突フラグ(要素：プレーヤー)
	int flgFtoE[][] = {{0,0,0},{0,0,0},{0,0,0},{0,0,0}};	//各敵と各友情コンボの衝突フラグ(行：友情コンボ・プレーヤー、列：敵)
	int flgHeart = 0;
	int flgWin = 0;			//勝利判定フラグ（0：ゲーム続行、1：プレーヤー勝利、2：敵勝利）
	int switchPE = 0;		//スイッチ（0：プレーヤーターン、1:敵ターン、2：ゲーム終了）
	int order = 0;			//プレーヤーの順番
	int hpPAll=0,hpP;		//プレーヤー全HP、現在HP

	Player pl[] = new Player[PLAYERS];	//プレーヤーのインスタンス
	Enemy ene[] = new Enemy[ENEMIES];	//敵のインスタンス
	Heart ht = new Heart();				//ハートのインスタンス

	//コンストラクタ
	public MonsterStrike(String title){
		//フレームの準備
		setTitle(title);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		add(pn);

		//プレーヤーの初期化（順番、速度、タイプ、HP、攻撃力、友情攻撃力、x座標、y座標）
		pl[0] = new Player(0,20,0,1230,1260,1897,40,350);
		pl[1] = new Player(1,18,1,960,1020,1440,120,350);
		pl[2] = new Player(2,18,1,970,1070,1280,200,350);
		pl[3] = new Player(3,21.4,0,920,820,2372,280,350);
		//プレーヤーのHP設定
		for(int j=0;j<PLAYERS;j++){
			hpPAll += pl[j].hp;
		}
		hpP = hpPAll;

		//敵の初期化（順番、HP、攻撃、ターン数、x座標、y座標）
		ene[0] = new Enemy(0,10000,333,2,50,100);
		ene[1] = new Enemy(1,20000,777,4,200,250);
		ene[2] = new Enemy(2,10000,333,2,270,50);
	}

	//メイン関数
	public static void main(String[] args) {
		//フレームの生成
		MonsterStrike frm = new MonsterStrike("モンスターストライク風ゲーム");
		frm.setSize(SIZEWINX,SIZEWINY);
		frm.setVisible(true);
	}

	//パネルクラス
	class MsPanel extends JPanel{

		PlayerAttack patt = new PlayerAttack();	//プレーヤー攻撃のタイマー
		Timer timer = new Timer(15,patt);		//タイマーのインスタンス。15ミリ秒間隔で呼出

		EnemyAttack eatt = new EnemyAttack();	//敵攻撃のタイマー
		Timer timer2 = new Timer(15,eatt);		//タイマーのインスタンス。15ミリ秒間隔で呼出

		//マウスリスナーの登録
		MsPanel(){
			addMouseListener(new MsMouseListener());
			timer2.setInitialDelay(1000);
		}

		//パネルの描画メソッド
		public void paintComponent(Graphics g){
			super.paintComponent(g);

			//背景色
			setBackground(new Color(0,150,100));

			//各プレーヤーの初期描画設定
			g.setColor(Color.blue);
			g.fillOval(pl[0].x,pl[0].y,SIZEP,SIZEP);
			g.setColor(Color.red);
			g.fillOval(pl[1].x,pl[1].y,SIZEP,SIZEP);
			g.setColor(Color.green);
			g.fillOval(pl[2].x,pl[2].y,SIZEP,SIZEP);
			g.setColor(Color.yellow);
			g.fillOval(pl[3].x,pl[3].y,SIZEP,SIZEP);
			//順番のプレーヤーを目立たせる
			if(switchPE == 0){
				g.setColor(Color.white);
				g.drawOval(pl[order].x,pl[order].y,SIZEP,SIZEP);
			}
			//プレーヤーのHP表示
			g.setColor(Color.white);
			g.setFont(new Font("SansSerif",Font.PLAIN,(int)(SIZEFONT*1.5)));
			g.drawString(hpP+" / "+hpPAll,0,450);

			g.setFont(new Font("SansSerif",Font.PLAIN,SIZEFONT));
			//敵の初期描画設定
			for(int i=0;i<ENEMIES;i++){
				if(ene[i].hp > 0){
					//敵生成
					g.setColor(Color.black);
					g.fillRect(ene[i].x,ene[i].y,SIZEE,SIZEE);
					//HP表示、ターン表示
					g.setColor(Color.white);
					g.drawString(ene[i].hp+" / "+ene[i].hpAll,ene[i].x,ene[i].y+SIZEE+SIZEFONT);
					g.drawString(Integer.toString(ene[i].turn),ene[i].x,ene[i].y);
					//敵攻撃表示
					if(switchPE==1 && ene[i].turn==0){
						g.setColor(Color.orange);
						g.fillRect(ene[i].x+SIZEE/4,0,SIZEE/2,SIZEWINY);
					}
				}
			}

			//友情コンボ表示
			for(int j=0;j<PLAYERS;j++){
				if(flgFtoP[j] == 1){
					g.setColor(Color.cyan);
					g.fillRect(pl[j].x+SIZEP/4,0,SIZEP/2,SIZEWINY);
				}
			}
			//クリックした場所を目立たせる
			if(flgTouch == 1){
				g.setColor(Color.white);
				g.fillOval((int)px-3,(int)py-3,6,6);
			}
			//ハート表示
			if(flgHeart == 1){
				g.setColor(Color.pink);
				g.fillArc(ht.x,ht.y,SIZEH,SIZEH,180,270);
			}

			//勝利・敗北表示
			if(flgWin == 1){
				//プレーヤー勝利
				g.setFont(new Font("SansSerif",Font.PLAIN,SIZEFONT*3));
				g.setColor(Color.orange);
				g.drawString("Stage Clear!",60,230);
			} else
			if(flgWin == 2){
				//プレーヤー敗北
				g.setFont(new Font("SansSerif",Font.PLAIN,SIZEFONT*3));
				g.setColor(Color.cyan);
				g.drawString("You Lose",80,230);
			}
		}

		//マウスリスナークラス
		class MsMouseListener implements MouseListener{
			public void mouseClicked(MouseEvent me){}
			public void mouseEntered(MouseEvent me){}
			public void mouseExited(MouseEvent me){}

			//クリックされた時の処理
			public void mousePressed(MouseEvent me){
				if(switchPE == 0){
					//クリック座標の取得
					px = me.getX();
					py = me.getY();
					//順番のプレーヤーがクリックされたらフラグをオンに
					if((px>=pl[order].x && px<=pl[order].x+SIZEP)&&(py>=pl[order].y && py<=pl[order].y+SIZEP)){
						flgTouch = 1;
						repaint();
					}
					for(int i=0;i<ENEMIES;i++){
						//敵との衝突フラグを戻す
						flgPAtt[i] = 0;
					}
				}
			}

			//クリックが放された時の処理
			public void mouseReleased(MouseEvent me){
				//順番のプレーヤーがクリックされていたら
				if(flgTouch == 1){
					//フラグを元に戻す
					flgTouch = 0;
					//クリックが放された座標を取得
					fx = me.getX();
					fy = me.getY();

					//速度計算
					rx = px - fx;
					ry = py - fy;
					rl = Math.sqrt((rx * rx + ry * ry));
					rate = pl[order].speed / rl;
					sx = rx * rate;
					sy = ry * rate;
					//減速計算
					mx = sx / (pl[order].speed * pl[order].speed);
					my = sy / (pl[order].speed * pl[order].speed);
					//プレーヤーターンのタイマー開始
					timer.start();
				}
			}
		}

		//プレーヤー攻撃（タイマー処理）クラス
		class PlayerAttack implements ActionListener{
			public void actionPerformed(ActionEvent e){

				int pex,pey;	//敵とプレーヤーの距離
				double pel;
				int ppx,ppy;	//プレーヤー同士の距離
				double ppl;
				int phx,phy;	//ハートとプレーヤーの距離
				double phl;

				//速度から、プレーヤー座標の更新
				pl[order].x = (int)(pl[order].x+sx);
				pl[order].y = (int)(pl[order].y+sy);
				//再描画
				repaint();
				//減速処理
				speedDown(2);

				//壁反射（ウィンドウ外に出たら、速度・減速度を逆に）
				if((pl[order].x<0 && sx<0) || (pl[order].x>SIZEWINX-SIZEP && sx>0)){
					sx *= -1;
					mx *= -1;
					speedDown(3);
				}else
				if((pl[order].y<0 && sy<0) || (pl[order].y>SIZEWINY-SIZEP-ERRORWINY && sy>0)){
					sy *= -1;
					my *= -1;
					speedDown(3);
				}

				//各敵との反射
				for(int i=0;i<ENEMIES;i++){
					if(ene[i].hp > 0){
						//プレーヤーと敵との距離を計算
						pex = Math.abs(pl[order].x - ene[i].x);
						pey = Math.abs(pl[order].y - ene[i].y);
						pel = Math.sqrt((double)pex * pex + (double)pey * pey);
						//プレーヤーと敵が衝突したならば
						if(pel <= ((SIZEP+SIZEE)/2)){
							//敵への攻撃
							if(flgPAtt[i]==0){
								ene[i].hp -= pl[order].att;
								if(ene[i].hp < 0){
									ene[i].hp = 0;
								}
								flgPAtt[i] = 1;
							}
							//プレーヤーが反射ならば
							if(pl[order].type == 0){
								if(pex >= pey){
									//左右反射
									if((pl[order].x>=ene[i].x && sx<0) || (pl[order].x<ene[i].x && sx>0)){
										sx *= -1;
										mx *= -1;
										//減速
										speedDown(5);
									}
								} else {
									//上下反射
									if((pl[order].y>=ene[i].y && sy<0) || (pl[order].y<ene[i].y && sy>0)){
										sy *= -1;
										my *= -1;
										//減速
										speedDown(5);
									}
								}
							} else {
								//プレーヤーが貫通ならば、より減速
								speedDown(8);
							}
						} else {
							//衝突していなければ、衝突フラグを元に戻す
							flgPAtt[i] = 0;
						}
					}
				}
				//各プレーヤーとの衝突・友情コンボ
				for(int j=0;j<PLAYERS;j++){
					if(j != order){	//順番のプレーヤー以外が友情コンボ対象
						//プレーヤー同士の距離を計算
						ppx = Math.abs(pl[order].x - pl[j].x);
						ppy = Math.abs(pl[order].y - pl[j].y);
						ppl = Math.sqrt((double)ppx * ppx + (double)ppy * ppy);
						//プレーヤー同士が衝突したら
						if(ppl<=SIZEP && flgFtoP[j]==0){
							//友情コンボオン
							flgFtoP[j] = 1;
							for(int i=0;i<ENEMIES;i++){
								//友情コンボと敵が衝突したら
								if((ene[i].x>pl[j].x+SIZEP/4-SIZEE)&&ene[i].x<pl[j].x+SIZEP*3/4){
									//敵のHPを減らす
									if(flgFtoE[j][i]==0){
										ene[i].hp -= pl[j].friend;
										if(ene[i].hp < 0){
											ene[i].hp = 0;
										}
										flgFtoE[j][i] = 1;
									}
								}
							}
						}
					}
				}

				//プレーヤーとハートの衝突
				phx = Math.abs(pl[order].x - ht.x);
				phy = Math.abs(pl[order].y - ht.y);
				phl = Math.sqrt((double)phx * phx + (double)phy * phy);
				if(phl<=((SIZEP+SIZEH)/2) && flgHeart==1){
					flgHeart = 0;
					//HPの回復
					hpP += (pl[order].hp);
					if(hpP > hpPAll){
						hpP = hpPAll;
					}
				}

				//速度が0になったらタイマー終了
				if(sx == 0 && sy == 0){
					//各フラグの初期化
					for(int j=0;j<PLAYERS;j++){
						flgFtoP[j] = 0;
						for(int i=0;i<ENEMIES;i++){
							flgFtoE[j][i] = 0;
						}
					}
					//勝利判定
					flgWin = 1;
					for(int i=0;i<ENEMIES;i++){
						if(ene[i].hp > 0){
							flgWin = 0;
						}
					}
					//プレーヤー勝利時
					if(flgWin == 1){
						switchPE = 2;
						repaint();
						timer.stop();
					}else{
					//ゲーム続行時
						timer.stop();
						repaint();
						timer2.start();
					}
				}
			}

			//減速処理
			public void speedDown(double rateDown){
				//減速率を考慮
				double mrx = mx * rateDown;
				double mry = my * rateDown;

				//減速処理（最小値を下回ったら、強制的に0にする）
				if((mrx<=sx && sx<=0)||(mrx>=sx && sx>=0)){
					sx = 0;
				} else {
					sx -= mrx;
				}
				if((mry<=sy && sy<=0)||(mry>=sy && sy>=0)){
					sy = 0;
				} else {
					sy -= mry;
				}
			}
		}

		//敵の攻撃（タイマー処理２）クラス
		class EnemyAttack implements ActionListener{
			int eAttTime = 0;
			int turnHeart = 0;
			public void actionPerformed(ActionEvent e){
				//呼び出しの時のみ
				if(eAttTime == 0){
					//敵ターン開始・レーザーを表示状態へ
					switchPE = 1;
					//攻撃ターン減少
					for(int i=0;i<ENEMIES;i++){
						ene[i].turn--;
					}
				}
				//敵の攻撃
				for(int i=0;i<ENEMIES;i++){
					//敵のターンが0になったら
					if(ene[i].turn == 0){
						for(int j=0;j<PLAYERS;j++){
							if(ene[i].hp > 0){
								//敵の攻撃とプレーヤーの衝突判定
								if((pl[j].x>ene[i].x+SIZEE/4-SIZEP)&&pl[j].x<ene[i].x+SIZEE*3/4){
									//プレーヤーのHPを減らす
									if(flgEAtt[i][j]==0 && hpP>0){
										hpP -= ene[i].att;
										if(hpP < 0){
											hpP = 0;
										}
										flgEAtt[i][j] = 1;
									}
								}
							}
						}
					}
				}
				repaint();

				//しばらくのあいだレーザーを表示
				if(++eAttTime > 150){
					eAttTime = 0;
					//ターン数が0の時は元に戻す
					for(int i=0;i<ENEMIES;i++){
						if(ene[i].turn == 0){
							ene[i].turn = ene[i].turnMax;
						}
					}
					//ゲーム続行時
					if(hpP > 0){
						//プレーヤー順番の更新
						if(++order == PLAYERS){
							order = 0;
						}
						switchPE = 0;
						for(int i=0;i<ENEMIES;i++){
							for(int j=0;j<PLAYERS;j++){
								flgEAtt[i][j] = 0;
							}
						}
						//ハート生成
						creatHeart();
					} else {
						//プレーヤー敗北時
						switchPE = 2;
						flgWin = 2;
					}
					repaint();
					timer2.stop();
				}
			}

			//ハート生成クラス
			public void creatHeart(){
				if(flgHeart == 0){
					//ハートが取られてから1ターン後（次の次のターン）に出現
					if(turnHeart >= 1){
						flgHeart = 1;
						//ランダムで各端のどこかに出現
						int wall = rand.nextInt(4);
						if(wall == 0){
							//上端
							ht.x = rand.nextInt(SIZEWINX - SIZEH - ERRORWINY);
							ht.y = 0;
						}else if(wall == 1){
							//右端
							ht.x = SIZEWINX - SIZEH;
							ht.y = rand.nextInt(SIZEWINY - SIZEH);
						}else if(wall == 2){
							//下端
							ht.x = rand.nextInt(SIZEWINX - SIZEH);
							ht.y = SIZEWINY - SIZEH - ERRORWINY;
						}else if(wall == 3){
							//左端
							ht.x = 0;
							ht.y = rand.nextInt(SIZEWINY - SIZEH - ERRORWINY);
						}
						turnHeart = 0;
					} else {
						turnHeart++;
					}
				}
			}
		}
	}

	//各プレーヤークラス
	class Player {
		public int x;			//x座標
		public int y;			//y座標
		public int order;		//順番
		public double speed;	//速度
		public int type;		//タイプ
		public int hp;			//HP
		public int att;			//攻撃力
		public int friend;		//友情攻撃力

		public Player(int order,double speed,int type,int hp,int att,int friend,int x,int y){
			this.order = order;
			this.speed = speed;
			this.type = type;
			this.hp = hp;
			this.att = att;
			this.friend = friend;
			this.x = x;
			this.y = y;
		}
	}

	//各敵のクラス
	class Enemy{
		public int x;			//x座標
		public int y;			//y座標
		public int order;		//順番
		public int hpAll;		//全hp
		public int hp;			//hp
		public int att;			//攻撃力
		public int turnMax;		//ターン数
		public int turn;		//現在ターン数

		public Enemy(int order,int hpAll,int att,int turnMax,int x,int y){
			this.order = order;
			this.hpAll = hpAll;
			this.hp = this.hpAll;
			this.att = att;
			this.turnMax = turnMax;
			turn = this.turnMax;
			this.x = x;
			this.y = y;
		}
	}

	//ハートクラス
	class Heart{
		public int x = 0;		//x座標
		public int y = 0;		//y座標
	}
}
