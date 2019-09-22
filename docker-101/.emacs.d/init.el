(setq inhibit-splash-screen t)
(setq initial-scratch-message nil)
;;(tool-bar-mode -1)
;;(set-default-font "Monaco 12")
;;(setq mac-option-modifier 'super)
;;(setq mac-command-modifier 'meta)
(global-set-key "\M-c" 'copy-region-as-kill)
(global-set-key "\M-v" 'yank)
(global-set-key "\M-g" 'goto-line)


(setq elpy-rpc-python-command "python3")
(setq python-shell-interpreter "python3")

(require 'package)
(add-to-list 'package-archives
             '("melpa" . "https://melpa.org/packages/"))
(package-initialize)
;;(load-theme 'atom-one-dark t)

(custom-set-variables
 ;; custom-set-variables was added by Custom.
 ;; If you edit it by hand, you could mess it up, so be careful.
 ;; Your init file should contain only one such instance.
 ;; If there is more than one, they won't work right.
 '(custom-safe-themes
   (quote
    ("6dd2b995238b4943431af56c5c9c0c825258c2de87b6c936ee88d6bb1e577cb9" default)))
 '(package-selected-packages (quote (yaml-mode elpy go-mode ##))))
(custom-set-faces
 ;; custom-set-faces was added by Custom.
 ;; If you edit it by hand, you could mess it up, so be careful.
 ;; Your init file should contain only one such instance.
 ;; If there is more than one, they won't work right.
 )


(add-to-list 'load-path "~/.emacs.d/lisp/")

;;(elpy-enable)
