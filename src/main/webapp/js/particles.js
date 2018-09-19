/* exported particles */
/**
 * Script edited from https://www.jqueryscript.net/animation/Confetti-Animation-jQuery-Canvas-Confetti-js.html
 */
const particles = {

    unicode: undefined,

    maxParticles: 150,
    canvas: undefined,
    ctx: undefined,
    w: undefined,
    h: undefined,
    animationHandler: undefined,
    animationComplete: false,
    particles: undefined,
    angle: 0,
    tiltAngle: 0,
    active: undefined,
    timeout: undefined,
    confetti: undefined,
    speed: undefined,

    init: function (canvasId) {
        this.canvas = document.getElementById(canvasId);
        this.ctx = this.canvas.getContext('2d');
        this.W = window.innerWidth;
        this.H = window.innerHeight;
        this.canvas.width = this.W;
        this.canvas.height = this.H;

        $(window).resize(function () {
            particles.W = window.innerWidth;
            particles.H = window.innerHeight;
            particles.canvas.width = particles.W;
            particles.canvas.height = particles.H;
        });

        window.requestAnimFrame = (function () {
            return window.requestAnimationFrame || window.webkitRequestAnimationFrame || window.mozRequestAnimationFrame || window.oRequestAnimationFrame || window.msRequestAnimationFrame || function (callback) {
                return window.setTimeout(callback, 1000 / 60);
            };
        })();
    },

    show: function (time, effect) {
        if (this.timeout) {
            clearTimeout(this.timeout);
            this.timeout = undefined;
            particles.stop();
            setTimeout(function () {
                particles.show(time, effect);
            }, 100);
            return;
        }

        this.unicode = effect[0];
        this.confetti = effect[0] === null;
        this.speed = effect[1];

        this.start();
        this.timeout = setTimeout(function () {
            particles.requestStop();
        }, time);
    },

    start: function () {
        this.canvas.style.display = 'block';
        this.particles = [];
        this.active = true;
        this.animationComplete = false;
        for (let i = 0; i < this.maxParticles; i++) {
            let particleColor = this.particleColors.getColor();
            this.particles.push(new this.particle(particleColor));
        }
        this.loop();
    },

    requestStop: function () {
        this.active = false;
        clearTimeout(this.animationHandler);
    },

    stop: function () {
        this.animationComplete = true;
        if (this.ctx === undefined) return;
        this.ctx.clearRect(0, 0, this.W, this.H);
        this.canvas.style.display = 'none';
    },

    loop: function () {
        if (particles.animationComplete)
            return null;
        particles.animationHandler = window.requestAnimFrame(particles.loop);
        return particles.draw();
    },

    draw: function () {
        this.ctx.clearRect(0, 0, this.W, this.H);
        let results = [];
        for (let i = 0; i < this.maxParticles; i++) {
            results.push(this.particles[i].draw());
        }
        this.update();
        return results;
    },

    update: function () {
        let remainingFlakes = 0;
        let particle;
        this.angle += this.speed * 0.01;
        this.tiltAngle += this.speed * 0.1;

        for (let i = 0; i < this.maxParticles; i++) {
            particle = this.particles[i];
            if (this.animationComplete) return;

            if (!this.active && particle.y < -15) {
                particle.y = this.H + 100;
                continue;
            }

            this.stepParticle(particle, i);

            if (particle.y <= this.H) {
                remainingFlakes++;
            }

            this.checkForReposition(particle, i);
        }

        if (remainingFlakes === 0) {
            this.stop();
        }
    },

    stepParticle: function (particle, particleIndex) {

        particle.tiltAngle += this.speed * particle.tiltAngleIncremental;
        particle.y += this.speed * (Math.cos(this.angle + particle.d) + 3 + particle.r / 2) / 2;
        particle.x += this.speed * Math.sin(this.angle);
        particle.tilt = (Math.sin(particle.tiltAngle - (particleIndex / 3))) * 15;
    },

    checkForReposition: function (particle, index) {
        if ((particle.x > this.W + 20 || particle.x < -20 || particle.y > this.H) && this.active) {
            if (index % 5 > 0 || index % 2 === 0) //66.67% of the flakes
            {
                this.repositionParticle(particle, Math.random() * this.W, -10, Math.floor(Math.random() * 10) - 10);
            } else {
                if (Math.sin(this.angle) > 0) {
                    //Enter from the left
                    this.repositionParticle(particle, -5, Math.random() * this.H, Math.floor(Math.random() * 10) - 10);
                } else {
                    //Enter from the right
                    this.repositionParticle(particle, this.W + 5, Math.random() * this.H, Math.floor(Math.random() * 10) - 10);
                }
            }
        }
    },

    repositionParticle: function (particle, xCoordinate, yCoordinate, tilt) {
        particle.x = xCoordinate;
        particle.y = yCoordinate;
        particle.tilt = tilt;
    },

    particleColors: {
        colorOptions: ['DodgerBlue', 'OliveDrab', 'Gold', 'pink', 'SlateBlue', 'lightblue', 'Violet', 'PaleGreen', 'SteelBlue', 'SandyBrown', 'Chocolate', 'Crimson'],
        colorIndex: 0,
        colorIncrementer: 0,
        colorThreshold: 10,
        getColor: function () {
            if (this.colorIncrementer >= 10) {
                this.colorIncrementer = 0;
                this.colorIndex++;
                if (this.colorIndex >= this.colorOptions.length) {
                    this.colorIndex = 0;
                }
            }
            this.colorIncrementer++;
            return this.colorOptions[this.colorIndex];
        }
    },

    particle: function (color) {
        this.x = Math.random() * particles.W; // x-coordinate
        this.y = (Math.random() * particles.H) - particles.H; //y-coordinate
        this.r = utils.randInt(10, 30); //radius;
        this.color = color;
        this.d = (Math.random() * particles.maxParticles) + 10; //density;
        this.tilt = Math.floor(Math.random() * 10) - 10;
        this.tiltAngleIncremental = (Math.random() * 0.07) + 0.05;
        this.tiltAngle = 0;
        if (particles.confetti) {
            this.draw = function () {
                particles.ctx.beginPath();
                particles.ctx.lineWidth = this.r / 2;
                particles.ctx.strokeStyle = this.color;
                particles.ctx.moveTo(this.x + this.tilt + (this.r / 4), this.y);
                particles.ctx.lineTo(this.x + this.tilt, this.y + this.tilt + (this.r / 4));
                return particles.ctx.stroke();
            };
        } else {
            this.draw = function () {
                particles.ctx.font = '900 ' + this.r.toFixed(0) + 'px \'Font Awesome 5 Free\'';
                particles.ctx.strokeStyle = this.color;
                particles.ctx.fillStyle = this.color;
                return particles.ctx.fillText(particles.unicode, this.x, this.y);
            };
        }
    },
};