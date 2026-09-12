/**
 * Scrobby — The Last.fm Scrobbler Engine for Caspian Flow
 * Ported from Scrobby (https://github.com/code4nigel/Scrobby---The-LastFM-Scrobbler-Extension-)
 * Authors: Shivanshu Yadav (code4nigel)
 */
(function(window) {
  'use strict';

  // Embedded RFC 1321 MD5 Implementation for API Signature Generation
  function md5(string) {
    function rotateLeft(lValue, iShiftBits) {
      return (lValue << iShiftBits) | (lValue >>> (32 - iShiftBits));
    }
    function addUnsigned(lX, lY) {
      var lX4 = (lX & 0x40000000);
      var lY4 = (lY & 0x40000000);
      var lX8 = (lX & 0x80000000);
      var lY8 = (lY & 0x80000000);
      var lResult = (lX & 0x3FFFFFFF) + (lY & 0x3FFFFFFF);
      if (lX4 & lY4) return (lResult ^ 0x80000000 ^ lX8 ^ lY8);
      if (lX4 | lY4) {
        if (lResult & 0x40000000) return (lResult ^ 0xC0000000 ^ lX8 ^ lY8);
        else return (lResult ^ 0x40000000 ^ lX8 ^ lY8);
      } else {
        return (lResult ^ lX8 ^ lY8);
      }
    }
    function F(x, y, z) { return (x & y) | ((~x) & z); }
    function G(x, y, z) { return (x & z) | (y & (~z)); }
    function H(x, y, z) { return (x ^ y ^ z); }
    function I(x, y, z) { return (y ^ (x | (~z))); }
    function FF(a, b, c, d, x, s, ac) {
      a = addUnsigned(a, addUnsigned(addUnsigned(F(b, c, d), x), ac));
      return addUnsigned(rotateLeft(a, s), b);
    }
    function GG(a, b, c, d, x, s, ac) {
      a = addUnsigned(a, addUnsigned(addUnsigned(G(b, c, d), x), ac));
      return addUnsigned(rotateLeft(a, s), b);
    }
    function HH(a, b, c, d, x, s, ac) {
      a = addUnsigned(a, addUnsigned(addUnsigned(H(b, c, d), x), ac));
      return addUnsigned(rotateLeft(a, s), b);
    }
    function II(a, b, c, d, x, s, ac) {
      a = addUnsigned(a, addUnsigned(addUnsigned(I(b, c, d), x), ac));
      return addUnsigned(rotateLeft(a, s), b);
    }
    function convertToWordArray(string) {
      var lWordCount;
      var lMessageLength = string.length;
      var lNumberOfWords_temp1 = lMessageLength + 8;
      var lNumberOfWords_temp2 = (lNumberOfWords_temp1 - (lNumberOfWords_temp1 % 64)) / 64;
      var lNumberOfWords = (lNumberOfWords_temp2 + 1) * 16;
      var lWordArray = Array(lNumberOfWords - 1);
      var lBytePosition = 0;
      var lByteCount = 0;
      while (lByteCount < lMessageLength) {
        lWordCount = (lByteCount - (lByteCount % 4)) / 4;
        lBytePosition = (lByteCount % 4) * 8;
        lWordArray[lWordCount] = (lWordArray[lWordCount] | (string.charCodeAt(lByteCount) << lBytePosition));
        lByteCount++;
      }
      lWordCount = (lByteCount - (lByteCount % 4)) / 4;
      lBytePosition = (lByteCount % 4) * 8;
      lWordArray[lWordCount] = lWordArray[lWordCount] | (0x80 << lBytePosition);
      lWordArray[lNumberOfWords - 2] = lMessageLength << 3;
      lWordArray[lNumberOfWords - 1] = lMessageLength >>> 29;
      return lWordArray;
    }
    function wordToHex(lValue) {
      var WordToHexValue = '', WordToHexValue_temp = '', lByte, lCount;
      for (lCount = 0; lCount <= 3; lCount++) {
        lByte = (lValue >>> (lCount * 8)) & 255;
        WordToHexValue_temp = '0' + lByte.toString(16);
        WordToHexValue = WordToHexValue + WordToHexValue_temp.substr(WordToHexValue_temp.length - 2, 2);
      }
      return WordToHexValue;
    }
    function utf8Encode(string) {
      string = string.replace(/\r\n/g, '\n');
      var utftext = '';
      for (var n = 0; n < string.length; n++) {
        var c = string.charCodeAt(n);
        if (c < 128) {
          utftext += String.fromCharCode(c);
        } else if ((c > 127) && (c < 2048)) {
          utftext += String.fromCharCode((c >> 6) | 192);
          utftext += String.fromCharCode((c & 63) | 128);
        } else {
          utftext += String.fromCharCode((c >> 12) | 224);
          utftext += String.fromCharCode(((c >> 6) & 63) | 128);
          utftext += String.fromCharCode((c & 63) | 128);
        }
      }
      return utftext;
    }

    var x = Array();
    var k, AA, BB, CC, DD, a, b, c, d;
    var S11 = 7, S12 = 12, S13 = 17, S14 = 22;
    var S21 = 5, S22 = 9, S23 = 14, S24 = 20;
    var S31 = 4, S32 = 11, S33 = 16, S34 = 23;
    var S41 = 6, S42 = 10, S43 = 15, S44 = 21;

    string = utf8Encode(string);
    x = convertToWordArray(string);
    a = 0x67452301; b = 0xEFCDAB89; c = 0x98BADCFE; d = 0x10325476;

    for (k = 0; k < x.length; k += 16) {
      AA = a; BB = b; CC = c; DD = d;
      a = FF(a, b, c, d, x[k + 0], S11, 0xD76AA478);
      d = FF(d, a, b, c, x[k + 1], S12, 0xE8C7B756);
      c = FF(c, d, a, b, x[k + 2], S13, 0x242070DB);
      b = FF(b, c, d, a, x[k + 3], S14, 0xC1BDCEEE);
      a = FF(a, b, c, d, x[k + 4], S11, 0xF57C0FAF);
      d = FF(d, a, b, c, x[k + 5], S12, 0x4787C62A);
      c = FF(c, d, a, b, x[k + 6], S13, 0xA8304613);
      b = FF(b, c, d, a, x[k + 7], S14, 0xFD469501);
      a = FF(a, b, c, d, x[k + 8], S11, 0x698098D8);
      d = FF(d, a, b, c, x[k + 9], S12, 0x8B44F7AF);
      c = FF(c, d, a, b, x[k + 10], S13, 0xFFFF5BB1);
      b = FF(b, c, d, a, x[k + 11], S14, 0x895CD7BE);
      a = FF(a, b, c, d, x[k + 12], S11, 0x6B901122);
      d = FF(d, a, b, c, x[k + 13], S12, 0xFD987193);
      c = FF(c, d, a, b, x[k + 14], S13, 0xA679438E);
      b = FF(b, c, d, a, x[k + 15], S14, 0x49B40821);

      a = GG(a, b, c, d, x[k + 1], S21, 0xF61E2562);
      d = GG(d, a, b, c, x[k + 6], S22, 0xC040B340);
      c = GG(c, d, a, b, x[k + 11], S23, 0x265E5A51);
      b = GG(b, c, d, a, x[k + 0], S24, 0xE9B6C7AA);
      a = GG(a, b, c, d, x[k + 5], S21, 0xD62F105D);
      d = GG(d, a, b, c, x[k + 10], S22, 0x2441453);
      c = GG(c, d, a, b, x[k + 15], S23, 0xD8A1E681);
      b = GG(b, c, d, a, x[k + 4], S24, 0xE7D3FBC8);
      a = GG(a, b, c, d, x[k + 9], S21, 0x21E1CDE6);
      d = GG(d, a, b, c, x[k + 14], S22, 0xC33707D6);
      c = GG(c, d, a, b, x[k + 3], S23, 0xF4D50D87);
      b = GG(b, c, d, a, x[k + 8], S24, 0x455A14ED);
      a = GG(a, b, c, d, x[k + 13], S21, 0xA9E3E905);
      d = GG(d, a, b, c, x[k + 2], S22, 0xFCEFA3F8);
      c = GG(c, d, a, b, x[k + 7], S23, 0x676F02D9);
      b = GG(b, c, d, a, x[k + 12], S24, 0x8D2A4C8A);

      a = HH(a, b, c, d, x[k + 5], S31, 0xFFFA3942);
      d = HH(d, a, b, c, x[k + 8], S32, 0x8771F681);
      c = HH(c, d, a, b, x[k + 11], S33, 0x6D9D6122);
      b = HH(b, c, d, a, x[k + 14], S34, 0xFDE5380C);
      a = HH(a, b, c, d, x[k + 1], S31, 0xA4BEEA44);
      d = HH(d, a, b, c, x[k + 4], S32, 0x4BDECFA9);
      c = HH(c, d, a, b, x[k + 7], S33, 0xF6BB4B60);
      b = HH(b, c, d, a, x[k + 10], S34, 0xBEBFBC70);
      a = HH(a, b, c, d, x[k + 13], S31, 0x289B7EC6);
      d = HH(d, a, b, c, x[k + 0], S32, 0xEAA127FA);
      c = HH(c, d, a, b, x[k + 3], S33, 0xD4EF3085);
      b = HH(b, c, d, a, x[k + 6], S34, 0x4881D05);
      a = HH(a, b, c, d, x[k + 9], S31, 0xD9D4D039);
      d = HH(d, a, b, c, x[k + 12], S32, 0xE6DB99E5);
      c = HH(c, d, a, b, x[k + 15], S33, 0x1FA27CF8);
      b = HH(b, c, d, a, x[k + 2], S34, 0xC4AC5665);

      a = II(a, b, c, d, x[k + 0], S41, 0xF4292244);
      d = II(d, a, b, c, x[k + 7], S42, 0x432AFF97);
      c = II(c, d, a, b, x[k + 14], S43, 0xAB9423A7);
      b = II(b, c, d, a, x[k + 5], S44, 0xFC93A039);
      a = II(a, b, c, d, x[k + 12], S41, 0x655B59C3);
      d = II(d, a, b, c, x[k + 3], S42, 0x8F0CCC92);
      c = II(c, d, a, b, x[k + 10], S43, 0xFFEFF47D);
      b = II(b, c, d, a, x[k + 1], S44, 0x85845DD1);
      a = II(a, b, c, d, x[k + 8], S41, 0x6FA87E4F);
      d = II(d, a, b, c, x[k + 15], S42, 0xFE2CE6E0);
      c = II(c, d, a, b, x[k + 6], S43, 0xA3014314);
      b = II(b, c, d, a, x[k + 13], S44, 0x4E0811A1);
      a = II(a, b, c, d, x[k + 4], S41, 0xF7537E82);
      d = II(d, a, b, c, x[k + 11], S42, 0xBD3AF235);
      c = II(c, d, a, b, x[k + 2], S43, 0x2AD7D2BB);
      b = II(b, c, d, a, x[k + 9], S44, 0xEB86D391);

      a = addUnsigned(a, AA);
      b = addUnsigned(b, BB);
      c = addUnsigned(c, CC);
      d = addUnsigned(d, DD);
    }
    return (wordToHex(a) + wordToHex(b) + wordToHex(c) + wordToHex(d)).toLowerCase();
  }

  // Scrobby Core Constants
  var API_KEY = 'f8409386dcfd73d2ff6db6f89093b137';
  var SHARED_SECRET = '87a9cc5c3b9b4b3b2c286db50239cf3d';
  var GITHUB_URL = 'https://github.com/code4nigel/Scrobby---The-LastFM-Scrobbler-Extension-';
  var API_URL = 'https://ws.audioscrobbler.com/2.0/';

  // Internal State
  var currentSong = {
    title: '',
    artist: '',
    album: '',
    duration: 0,
    currentTime: 0,
    paused: true,
    artwork: '',
    accumulatedTime: 0,
    lastUpdate: null,
    scrobbled: false,
    nowPlayingSent: false,
    startTimestamp: 0,
    sourceSite: '',
    repeatCount: 0
  };

  var stateChangeListeners = [];

  function emitState() {
    var copy = JSON.parse(JSON.stringify(currentSong));
    stateChangeListeners.forEach(function(cb) {
      try { cb(copy); } catch (e) {}
    });
  }

  // Settings & Preferences
  function getSettings() {
    var settings = {};
    try {
      settings = JSON.parse(localStorage.getItem('caspian_scrobby_settings') || '{}');
    } catch (e) {}

    return {
      enabled: settings.enabled !== false,
      sessionKey: localStorage.getItem('caspian_scrobby_session_key') || '',
      username: localStorage.getItem('caspian_scrobby_username') || '',
      cleanRemasters: settings.cleanRemasters !== false,
      primaryArtistOnly: settings.primaryArtistOnly !== false,
      scrobbleMode: settings.scrobbleMode || 'percent',
      scrobblePercent: parseInt(settings.scrobblePercent, 10) || 50,
      scrobbleSeconds: parseInt(settings.scrobbleSeconds, 10) || 240,
      customRegexRules: settings.customRegexRules || []
    };
  }

  function saveSettings(updates) {
    var current = getSettings();
    var merged = Object.assign({}, current, updates);
    localStorage.setItem('caspian_scrobby_settings', JSON.stringify({
      enabled: merged.enabled,
      cleanRemasters: merged.cleanRemasters,
      primaryArtistOnly: merged.primaryArtistOnly,
      scrobbleMode: merged.scrobbleMode,
      scrobblePercent: merged.scrobblePercent,
      scrobbleSeconds: merged.scrobbleSeconds,
      customRegexRules: merged.customRegexRules
    }));
    if (typeof updates.sessionKey === 'string') {
      localStorage.setItem('caspian_scrobby_session_key', updates.sessionKey);
    }
    if (typeof updates.username === 'string') {
      localStorage.setItem('caspian_scrobby_username', updates.username);
    }
    emitState();
  }

  // API Signature Generator
  function generateSignature(params, secret) {
    var sortedKeys = Object.keys(params).sort();
    var sigString = '';
    for (var i = 0; i < sortedKeys.length; i++) {
      var key = sortedKeys[i];
      if (key === 'api_sig' || key === 'format') continue;
      sigString += key + params[key];
    }
    sigString += secret;
    return md5(sigString);
  }

  // Last.fm API Caller
  async function callLastFm(method, params, requiresAuth) {
    if (typeof requiresAuth === 'undefined') requiresAuth = true;
    var settings = getSettings();
    if (requiresAuth && !settings.sessionKey) {
      throw new Error('Not authenticated with Last.fm');
    }

    var finalParams = Object.assign({
      method: method,
      api_key: API_KEY
    }, params);

    if (requiresAuth) {
      finalParams.sk = settings.sessionKey;
    }

    finalParams.api_sig = generateSignature(finalParams, SHARED_SECRET);
    finalParams.format = 'json';

    var isPost = (method === 'track.scrobble' || method === 'track.updateNowPlaying' || method === 'auth.getSession');

    var res;
    if (isPost) {
      var body = new URLSearchParams();
      for (var k in finalParams) {
        body.append(k, finalParams[k]);
      }
      res = await fetch(API_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body
      });
    } else {
      var q = new URLSearchParams(finalParams).toString();
      res = await fetch(API_URL + '?' + q);
    }

    var data = await res.json();
    if (data.error) {
      throw new Error(data.message || ('Last.fm Error ' + data.error));
    }
    return data;
  }

  // Authenticate with Token
  async function authenticateWithToken(token) {
    try {
      var res = await callLastFm('auth.getSession', { token: token }, false);
      if (res && res.session) {
        saveSettings({
          sessionKey: res.session.key,
          username: res.session.name
        });
        return { success: true, username: res.session.name };
      }
      return { success: false, error: 'No session object returned' };
    } catch (e) {
      return { success: false, error: e.message };
    }
  }

  function startAuthFlow() {
    var cbUrl = 'https://music.youtube.com/lastfm-callback';
    var authUrl = 'https://www.last.fm/api/auth/?api_key=' + API_KEY + '&cb=' + encodeURIComponent(cbUrl);
    if (window.CaspianBridge && typeof window.CaspianBridge.openExternalUrl === 'function') {
      window.CaspianBridge.openExternalUrl(authUrl);
    } else if (window.CaspianBridge && typeof window.CaspianBridge.openUrl === 'function') {
      window.CaspianBridge.openUrl(authUrl);
    } else {
      window.open(authUrl, '_blank');
    }
  }

  function disconnect() {
    localStorage.removeItem('caspian_scrobby_session_key');
    localStorage.removeItem('caspian_scrobby_username');
    emitState();
  }

  // Preprocess and Clean Track Metadata (from Scrobby)
  function cleanTrackInfo(title, artist, settings) {
    var cleanTitle = (title || '').trim();
    var cleanArtist = (artist || '').trim();

    // 1. Primary Artist Only (removes feat., ft., &, vs.)
    if (settings.primaryArtistOnly && cleanArtist) {
      var splitRegex = /\s*(?:,|\bfeat\.?|\bft\.?|&|\band\b|\bvs\.?)\s+/i;
      cleanArtist = cleanArtist.split(splitRegex)[0].trim();
    }

    // 2. Clean Remaster / Live / Video Noise
    if (settings.cleanRemasters && cleanTitle) {
      var noiseRegex = /\s*([\[\(])\s*(official|lyric|video|music|hd|mv|audio|live|remix|version|4k|raw|visualizer|clip|performance|soundtrack|ost|audio\s*track|explicit|clean).*?([\]\)])/gi;
      cleanTitle = cleanTitle.replace(noiseRegex, '').trim();

      var remasterRegex = /\s*[-–(]\s*\d*(?:st|nd|rd|th)?\s*(?:anniversary|remastered|remaster|live|bonus track|deluxe).*?[)\]]?/gi;
      cleanTitle = cleanTitle.replace(remasterRegex, '').trim();
      cleanTitle = cleanTitle.replace(/\s*[-–]\s*$/g, '').trim();
    }

    // 3. Custom Regex Rules
    if (settings.customRegexRules && settings.customRegexRules.length > 0) {
      for (var i = 0; i < settings.customRegexRules.length; i++) {
        var rule = settings.customRegexRules[i];
        if (!rule || !rule.find) continue;
        try {
          var r = new RegExp(rule.find, 'gi');
          cleanTitle = cleanTitle.replace(r, rule.replace || '');
          cleanArtist = cleanArtist.replace(r, rule.replace || '');
        } catch (e) {}
      }
    }

    // Whitespace Normalization
    cleanTitle = cleanTitle.replace(/\s+/g, ' ').replace(/^["']|["']$/g, '').trim();
    cleanArtist = cleanArtist.replace(/\s+/g, ' ').replace(/^["']|["']$/g, '').trim();

    return { title: cleanTitle, artist: cleanArtist };
  }

  // Recent Scrobbles Cache
  function getRecentScrobbles() {
    try {
      return JSON.parse(localStorage.getItem('caspian_scrobby_recent') || '[]');
    } catch (e) {
      return [];
    }
  }

  function saveRecentScrobble(song) {
    var list = getRecentScrobbles();
    list.unshift({
      title: song.title,
      artist: song.artist,
      album: song.album,
      artwork: song.artwork,
      timestamp: Date.now()
    });
    if (list.length > 20) list.pop();
    localStorage.setItem('caspian_scrobby_recent', JSON.stringify(list));
  }

  // Now Playing
  async function sendNowPlaying(song) {
    var settings = getSettings();
    if (!settings.enabled || !settings.sessionKey || !song.title || !song.artist) return;
    try {
      await callLastFm('track.updateNowPlaying', {
        track: song.title,
        artist: song.artist,
        album: song.album || '',
        duration: Math.floor(song.duration || 0)
      });
    } catch (e) {
      console.warn('Scrobby: NowPlaying error:', e);
    }
  }

  // Scrobble
  async function submitScrobble(song) {
    var settings = getSettings();
    if (!settings.enabled || !settings.sessionKey || !song.title || !song.artist) return;
    try {
      await callLastFm('track.scrobble', {
        track: song.title,
        artist: song.artist,
        album: song.album || '',
        timestamp: song.startTimestamp || Math.floor(Date.now() / 1000)
      });
      saveRecentScrobble(song);
      emitState();
    } catch (e) {
      console.error('Scrobby: Scrobble error:', e);
    }
  }

  // Handle Player State from YTM
  async function handlePlayerState(data) {
    if (!data || !data.title) return;
    var settings = getSettings();
    if (!settings.enabled) return;

    var cleaned = cleanTrackInfo(data.title, data.artist, settings);
    var isNewSong = (currentSong.title !== cleaned.title || currentSong.artist !== cleaned.artist);

    if (isNewSong) {
      currentSong = {
        title: cleaned.title,
        artist: cleaned.artist,
        album: data.album || '',
        duration: data.duration || 0,
        currentTime: data.currentTime || 0,
        paused: !!data.paused,
        artwork: data.artwork || '',
        accumulatedTime: 0,
        lastUpdate: data.paused ? null : Date.now(),
        scrobbled: false,
        nowPlayingSent: false,
        startTimestamp: Math.floor(Date.now() / 1000),
        sourceSite: data.sourceSite || 'music.youtube.com',
        repeatCount: 0
      };

      if (!currentSong.paused) {
        sendNowPlaying(currentSong);
        currentSong.nowPlayingSent = true;
      }
    } else {
      // Loop & Repeat Multi-Scrobble Detection (from Scrobby)
      var isRepeated = (currentSong.scrobbled && data.currentTime < 10) ||
                         (data.currentTime < currentSong.currentTime - 10 && currentSong.currentTime > 15);

      if (isRepeated) {
        currentSong.repeatCount++;
        currentSong.accumulatedTime = 0;
        currentSong.scrobbled = false;
        currentSong.nowPlayingSent = false;
        currentSong.startTimestamp = Math.floor(Date.now() / 1000);
        currentSong.currentTime = data.currentTime;
        currentSong.lastUpdate = data.paused ? null : Date.now();

        if (!currentSong.paused) {
          sendNowPlaying(currentSong);
          currentSong.nowPlayingSent = true;
        }
      } else {
        if (!currentSong.paused && currentSong.lastUpdate) {
          var delta = (Date.now() - currentSong.lastUpdate) / 1000;
          if (delta > 0 && delta < 5) {
            currentSong.accumulatedTime += delta;
          }
        }
      }

      currentSong.currentTime = data.currentTime || 0;
      currentSong.duration = data.duration || currentSong.duration;
      currentSong.paused = !!data.paused;
      currentSong.artwork = data.artwork || currentSong.artwork;
      currentSong.album = data.album || currentSong.album;
      currentSong.lastUpdate = data.paused ? null : Date.now();

      if (!currentSong.paused && !currentSong.nowPlayingSent) {
        sendNowPlaying(currentSong);
        currentSong.nowPlayingSent = true;
      }
    }

    // Scrobble Threshold Enforcement
    if (!currentSong.scrobbled && currentSong.duration >= 30) {
      var threshold = 30;
      if (settings.scrobbleMode === 'percent') {
        threshold = currentSong.duration * (settings.scrobblePercent / 100);
      } else {
        threshold = settings.scrobbleSeconds;
      }
      threshold = Math.max(30, threshold);

      if (currentSong.accumulatedTime >= threshold) {
        currentSong.scrobbled = true;
        await submitScrobble(currentSong);
      }
    }

    emitState();
  }

  // Export to window
  window.ScrobbyEngine = {
    GITHUB_URL: GITHUB_URL,
    getState: function() { return JSON.parse(JSON.stringify(currentSong)); },
    getSettings: getSettings,
    saveSettings: saveSettings,
    startAuthFlow: startAuthFlow,
    authenticateWithToken: authenticateWithToken,
    disconnect: disconnect,
    handlePlayerState: handlePlayerState,
    onPlayerStateReceived: handlePlayerState,
    getRecentScrobbles: getRecentScrobbles,
    cleanTrackInfo: function(t, a) { return cleanTrackInfo(t, a, getSettings()); },
    onStateChange: function(cb) {
      if (typeof cb === 'function') stateChangeListeners.push(cb);
    }
  };

})(window);
